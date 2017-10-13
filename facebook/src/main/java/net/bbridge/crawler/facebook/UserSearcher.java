package net.bbridge.crawler.facebook;

import net.bbridge.crawler.exception.DataRetrievalException;
import net.bbridge.crawler.facebook.entities.User;
import net.bbridge.crawler.facebook.matchers.SearchPageMatcher;
import net.bbridge.crawler.utils.PageFetcher;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class UserSearcher {

    private PageFetcher pageFetcher;
    private SearchPageMatcher searchPageMatcher = new SearchPageMatcher();

    public UserSearcher(PageFetcher pageFetcher) {
        this.pageFetcher = pageFetcher;
    }

    public User findUserByEmail(String email) throws IOException, DataRetrievalException {
        String url = "https://www.facebook.com/search/top/?q=" + email;
        Document document = pageFetcher.getPageDocument(url);
        User user = searchPageMatcher.getUser(document);
        user.setEmail(email);
        return user;
    }
}
