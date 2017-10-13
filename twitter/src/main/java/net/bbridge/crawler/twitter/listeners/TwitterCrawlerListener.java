package net.bbridge.crawler.twitter.listeners;

import com.mongodb.BasicDBObject;
import net.bbridge.crawler.instagram.InstagramCrawler;
import net.bbridge.crawler.instagram.MediaExtractor;
import net.bbridge.crawler.twitter.entities.Tweet;
import net.bbridge.crawler.twitter.extractors.VenueExtractor;
import net.bbridge.crawler.utils.DataBaseAdapter;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kseniya on 29/12/16.
 */
public class TwitterCrawlerListener implements CrawlerListener<Tweet> {

    private static final Logger log = Logger.getLogger(TwitterCrawlerListener.class.getName());

    private final DataBaseAdapter dataBaseAdapter = new DataBaseAdapter();
    private final MediaExtractor mediaExtractor = new MediaExtractor();
    private final VenueExtractor venueExtractor = new VenueExtractor();
    private final ExecutorService service = Executors.newFixedThreadPool(4);
    private final InstagramCrawler instagramCrawler = new InstagramCrawler();
    private final HashSet<Long> processedUsers = new HashSet<>();

    @Override
    public void onMessageReceived(Tweet tweet) {
        switch (tweet.getSource()) {
            case FOURSQUARE:
                service.submit(() -> venueExtractor.onTweetReceived(tweet));
                break;
            case INSTAGRAM:
                try {
                    if (!processedUsers.contains(tweet.getUserId())) {
                        log.info(String.format("Collecting instagram timeline for user [%s/%s] begins. User locked for future collections.",
                                tweet.getUserId(), tweet.getUsername()));
                        processedUsers.add(tweet.getUserId());
                        final BasicDBObject instagramUserInfo = mediaExtractor.onTweetReceived(tweet.getText());
                        dataBaseAdapter.updateObject(new BasicDBObject("_id", tweet.getUserId()), instagramUserInfo, "users");
                        service.submit(() -> instagramCrawler.start(Arrays.asList((String[]) instagramUserInfo.get("instagramUserName"))));
                    }
                } catch (Exception e) {
                    log.warn("Can't crawl instagram timeline, skipping", e);
                }
                break;
        }
        dataBaseAdapter.saveObjectToDataBase(createBasicObjectFromTweet(tweet), "tweets");
    }

    private BasicDBObject createBasicObjectFromTweet(Tweet t) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", t.getId());
        basicDBObject.put("link", t.getUrl());
        basicDBObject.put("userName", t.getUsername());
        basicDBObject.put("userId", t.getUserId());
        basicDBObject.put("text", t.getText());
        basicDBObject.put("date", t.getDate());
        basicDBObject.put("retweets", t.getRetweets());
        basicDBObject.put("favorites", t.getFavorites());
        basicDBObject.put("mentions", t.getMentions());
        basicDBObject.put("hashtag", t.getHashtags());
        basicDBObject.put("geo", t.getGeo());
        basicDBObject.put("type", t.getSource());
        return basicDBObject;
    }
}
