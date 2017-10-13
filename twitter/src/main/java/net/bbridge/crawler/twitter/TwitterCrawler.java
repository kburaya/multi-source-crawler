package net.bbridge.crawler.twitter;

import com.mongodb.BasicDBObject;
import net.bbridge.crawler.Crawler;
import net.bbridge.crawler.twitter.entities.Tweet;
import net.bbridge.crawler.twitter.entities.TweetSource;
import net.bbridge.crawler.twitter.entities.TwitterCredentials;
import net.bbridge.crawler.twitter.listeners.TwitterCrawlerListener;
import net.bbridge.crawler.utils.DataBaseAdapter;
import net.bbridge.crawler.utils.PageFetcher;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kseniya on 23/10/2016.
 */
public class TwitterCrawler implements Crawler {

    private static final Logger log = Logger.getLogger(TwitterCrawler.class.getName());
    private static final String TWITTER_URL = "https://twitter.com";

    private final TwitterCrawlerListener listener;
    private final DataBaseAdapter dataBaseAdapter;
    private final Twitter twitter;
    private final PageFetcher pageFetcher;

    public TwitterCrawler() {
        this.listener = new TwitterCrawlerListener();
        this.dataBaseAdapter = new DataBaseAdapter();
        this.twitter = new TwitterFactory().getInstance();
        this.pageFetcher = new PageFetcher();
    }

    @Override
    public void start(List<String> ids) {
        auth();
        for (String id : ids) {
            try {
                processUser(id);
            } catch (Exception e) {
                log.error("Cloudn't process user: " + id);
            }
        }
    }

    @Override
    public void stop() {
    }

    private void auth() {
        TwitterCredentials credentials = new TwitterCredentials();
        twitter.setOAuthConsumer(credentials.getConsumerKey(), credentials.getConsumerSecret());
        AccessToken accessToken = new AccessToken(credentials.getUserAccessToken(), credentials.getUserAccessSecret());
        twitter.setOAuthAccessToken(accessToken);
    }

    private void processUser(String id) throws Exception {
        BasicDBObject checkFieldsForUser = new BasicDBObject();
        checkFieldsForUser.put("_id", id);
        checkFieldsForUser.put("twitterTimeLineCollected", true);
        if (dataBaseAdapter.isObjectExists(checkFieldsForUser, "users")) {
            log.info(String.format("User %s already collected, skipping", id));
            return;
        }
        User user = twitter.showUser(Long.valueOf(id));
        log.info(String.format("Begin to collect tweets for user [%s]", user.getScreenName()));
        Map<TweetSource, Integer> stats = fetchTweets(user);
        BasicDBObject userDBO = statsToDBO(stats);

        userDBO.put("twitterUserName", user.getScreenName());
        userDBO.put("twitterTimeLineCollected", true);

        dataBaseAdapter.updateObject(new BasicDBObject("_id", id), userDBO, "users");
    }

    private Map<TweetSource, Integer> fetchTweets(User user) throws Exception {
        Map<TweetSource, Integer> stats = new EnumMap<>(TweetSource.class);
        for (TweetSource source : TweetSource.values()) {
            stats.put(source, 0);
        }
        boolean isFirstElement = true;
        String refreshCursor = null;
        while (true) {
            JSONObject timelineResponseJson = new JSONObject(requestTimelinePage(user.getScreenName(), refreshCursor));
            refreshCursor = timelineResponseJson.getString("min_position");
            Document timelineResponseDocument = Jsoup.parse(timelineResponseJson.getString("items_html"));
            Elements tweetElements = timelineResponseDocument.select("div.js-stream-tweet");

            if (tweetElements.isEmpty()) {
                break;
            }
            for (Element element : tweetElements) {
                String id = element.attr("data-tweet-id");
                if (dataBaseAdapter.isObjectExists(new BasicDBObject("_id", id), "tweets"))
                    continue;
                Tweet tweet = parseTweet(element, user.getId());
                listener.onMessageReceived(tweet);
                stats.put(tweet.getSource(), stats.get(tweet.getSource()) + 1);

                if (isFirstElement) {
                    saveLastTweetId(tweet);
                    isFirstElement = false;
                }
            }
            log.debug(String.format("Retrieved [%d] tweets for user [%s]",
                    tweetElements.size(), user.getScreenName()));
        }

        return stats;
    }

    private Tweet parseTweet(Element element, long userId) {
        String id = element.attr("data-tweet-id");
        String username = element.select("span.username.js-action-profile-name b").text();
        String tweetUrl = element.attr("data-permalink-path");
        String text = element.select("p.js-tweet-text")
                .text()
                .replaceAll("[^\\u0000-\\uFFFF]", "");
        TweetSource source = recognizeSource(text);

        long dateMs = Long.valueOf(
                element.select("small.time span.js-short-timestamp")
                        .attr("data-time-ms"));
        Date date = new Date(dateMs);

        int retweets = Integer.valueOf(
                element.select("span.ProfileTweet-action--retweet span.ProfileTweet-actionCount")
                        .attr("data-tweet-stat-count")
                        .replaceAll(",", ""));
        int favorites = Integer.valueOf(
                element.select("span.ProfileTweet-action--favorite span.ProfileTweet-actionCount")
                        .attr("data-tweet-stat-count")
                        .replaceAll(",", ""));

        Elements geoElement = element.select("span.Tweet-geo");
        String geo;
        if (!geoElement.isEmpty()) {
            geo = geoElement.attr("title");
        } else {
            geo = null;
        }

        return new Tweet(
                id,
                userId,
                username,
                TWITTER_URL + tweetUrl,
                source,
                text,
                date,
                retweets,
                favorites,
                findMatches("(@\\w*)", text),
                findMatches("(#\\w*)", text),
                geo);
    }

    private TweetSource recognizeSource(String text) {
        if (text.contains("4sq.com") || text.contains("swarmapp.com") || text.contains("foursquare.com")) {
            return TweetSource.FOURSQUARE;
        } else if (text.contains("instagram.com") || text.contains("instagr.am")) {
            return TweetSource.INSTAGRAM;
        } else {
            return TweetSource.TWITTER;
        }
    }

    String requestTimelinePage(String userName, String scrollCursor) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(TWITTER_URL);
        uriBuilder.setPath("/i/search/timeline");
        uriBuilder.addParameter("f", "realtime");
        uriBuilder.addParameter("q", URLEncoder.encode("from:" + userName, "UTF-8"));
        uriBuilder.addParameter("src", "typd");
        uriBuilder.addParameter("max_position", scrollCursor);
        URL url = uriBuilder.build().toURL();

        return pageFetcher.getPageBody(url.toString());
    }

    private List<String> findMatches(String pattern, String text) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    private BasicDBObject statsToDBO(Map<TweetSource, Integer> stats) {
        int twitter = stats.get(TweetSource.TWITTER);
        int instagram = stats.get(TweetSource.INSTAGRAM);
        int foursquare = stats.get(TweetSource.FOURSQUARE);
        int total = twitter + instagram + foursquare;

        BasicDBObject result = new BasicDBObject();
        result.put("Twitter", twitter);
        result.put("Instagram", instagram);
        result.put("Foursquare", foursquare);
        result.put("tweetsCount", total);
        result.put("collected", "True");

        return result;
    }

    private void saveLastTweetId(Tweet tweet) {
        BasicDBObject lastTweetDBO = new BasicDBObject();
        lastTweetDBO.put("_id", tweet.getUserId());
        lastTweetDBO.put("id", tweet.getId());

        dataBaseAdapter.saveObjectToDataBase(lastTweetDBO, "last_tweet_id");
    }
}
