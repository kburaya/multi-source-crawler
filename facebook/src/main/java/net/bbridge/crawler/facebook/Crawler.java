package net.bbridge.crawler.facebook;

import lombok.extern.slf4j.Slf4j;
import net.bbridge.crawler.exception.DataRetrievalException;
import net.bbridge.crawler.facebook.entities.Post;
import net.bbridge.crawler.facebook.entities.User;
import net.bbridge.crawler.facebook.matchers.ConsPageMatcher;
import net.bbridge.crawler.facebook.matchers.FacebookPostsFetcher;
import net.bbridge.crawler.facebook.matchers.FirstPageMatcher;
import net.bbridge.crawler.utils.PageFetcher;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Crawler {

    private PageFetcher pageFetcher;
    private UserSearcher userSearcher;
    private FacebookPostsFetcher firstPageMatcher = new FirstPageMatcher();
    private FacebookPostsFetcher consPageMatcher = new ConsPageMatcher();

    public Crawler(PageFetcher pageFetcher, UserSearcher userSearcher) {
        this.pageFetcher = pageFetcher;
        this.userSearcher = userSearcher;
        log.info("Facebook service initialized");
    }

    public User retrieve(long userId, int postsLimit) {
        User user = new User(userId);
        user.setPosts(getPosts(user, postsLimit));
        return user;
    }

    public User retrieve(String email, int postsLimit) {
        User user = null;
        try {
            user = userSearcher.findUserByEmail(email);
            user.setPosts(getPosts(user, postsLimit));
        } catch (IOException | DataRetrievalException e) {
            log.error("Failed to find user by email", e);
        }
        return user;
    }

    private List<Post> getPosts(User user, int limit) {
        log.info("Getting posts of user ({}, {})", user.getId(), user.getUsername());
        List<Post> posts = new ArrayList<>();
        try {
            // getting the first page
            String url;
            if (user.getUsername() != null) {
                url = "https://facebook.com/" + user.getUsername();
            } else {
                url = "https://facebook.com/profile.php?id=" + user.getId();
            }
            Document curPageDocument = pageFetcher.getPageDocument(url);
            posts.addAll(firstPageMatcher.getPosts(curPageDocument));
            String ajaxPipeToken = getAjaxPipeToken(curPageDocument.html());

            // scrolling down
            while (posts.size() < limit) {
                String secondPageURL = getNextPageURL(curPageDocument, ajaxPipeToken);
                curPageDocument = pageFetcher.getPageDocument(secondPageURL);
                posts.addAll(consPageMatcher.getPosts(curPageDocument));
            }
        } catch (Exception e) {
            log.error("Failed to get posts of user ({}, {}): {}", user.getId(), user.getUsername(), e);
        }
        for (Post post : posts) {
            System.out.println("Post:");
            System.out.println(post.getText());
            post.getImages().forEach(System.out::println);
            System.out.println();
        }
        return posts;
    }

    private String getNextPageURL(Document curPageDocument, String ajaxPipeToken) throws DataRetrievalException, URISyntaxException {
        String curPageHtml = curPageDocument.html();
        String dataJson = findDataJson(curPageHtml);
        String nextPageUrl = constructNextPageUrl(ajaxPipeToken, dataJson);
        log.debug("Constructed URL:", nextPageUrl);
        return nextPageUrl;
    }

    private String constructNextPageUrl(String token, String data) throws URISyntaxException {
        URIBuilder builder = new URIBuilder("https://www.facebook.com/ajax/pagelet/generic.php/ProfileTimelineSectionPagelet");
        builder.addParameter("dpr", "1");
        builder.addParameter("ajaxpipe", "1");
        builder.addParameter("ajaxpipe_token", token);
        builder.addParameter("no_script_path", "1");
        builder.addParameter("data", data);
        return builder.build().toString();
    }

    private static final String PIPE_TOKEN_REGEX = "\"ajaxpipe_token\":\"([a-zA-Z0-9_-]*)\"";

    private String getAjaxPipeToken(String pageHtml) throws DataRetrievalException {
        Pattern pattern = Pattern.compile(PIPE_TOKEN_REGEX);
        Matcher matcher = pattern.matcher(pageHtml);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new DataRetrievalException("Could not find ajaxpipe_token");
        }
    }

    private static final String SCROLLING_PAGER_CREATION = "return new ScrollingPager(";

    private String findDataJson(String src) {
        int start = src.indexOf(SCROLLING_PAGER_CREATION) + SCROLLING_PAGER_CREATION.length();
        int end = src.indexOf(')', start);
        String jsccMapString = src.substring(start, end);
        String object = StringUtils.findFirstObject(jsccMapString);
        if (object != null) {
            return object.replace("\\\"", "\"");
        } else {
            return null;
        }
    }
}
