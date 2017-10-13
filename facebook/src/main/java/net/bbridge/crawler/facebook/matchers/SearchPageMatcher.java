package net.bbridge.crawler.facebook.matchers;

import net.bbridge.crawler.exception.DataRetrievalException;
import net.bbridge.crawler.facebook.entities.User;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchPageMatcher {

    private static final String CONTAINER_ID_REGEX = "content:\\{\"pagelet_loader_browse:independent:modules:pagelet\":\\{container_id:\"(\\w*)\"}},";
    private static final String USER_ID_REGEX = "&quot;id&quot;:(\\d*),";
    private static final String USERNAME_REGEX = "href=\"https://www.facebook.com/([a-zA-Z0-9.]*)\\?ref=br_rs\"";

    private ContainerMatcher containerMatcher;
    private Pattern userIdPattern;
    private Pattern usernamePattern;

    public SearchPageMatcher() {
        this.containerMatcher = new ContainerMatcher(CONTAINER_ID_REGEX);
        this.userIdPattern = Pattern.compile(USER_ID_REGEX);
        this.usernamePattern = Pattern.compile(USERNAME_REGEX);
    }

    public User getUser(Document document) throws DataRetrievalException {
        String searchResultData = containerMatcher.getContainerData(document);
        long userId = getUserId(searchResultData);
        String username = getUsername(searchResultData);
        return new User(userId, username);
    }

    private String getUsername(String searchResultData) throws DataRetrievalException {
        Matcher matcher = usernamePattern.matcher(searchResultData);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new DataRetrievalException("Cannot find username at the search page");
        }
    }

    private long getUserId(String searchResultData) throws DataRetrievalException {
        Matcher matcher = userIdPattern.matcher(searchResultData);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        } else {
            throw new DataRetrievalException("Cannot find userId at the search page");
        }
    }
}
