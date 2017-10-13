package net.bbridge.crawler.facebook.matchers;

import net.bbridge.crawler.exception.DataRetrievalException;
import net.bbridge.crawler.facebook.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsPageMatcher extends FacebookPostsFetcher {

    private static final String USER_CONTENT_REGEX = "\"payload\":\\{\"content\":\\{\"pagelet_timeline_recent_segment_.*story\":\"";

    @Override
    protected String getUserData(Document document) throws Exception {
        String pageHtml = StringUtils.replaceUnicode(document.html());
        Pattern pattern = Pattern.compile(USER_CONTENT_REGEX);
        Matcher matcher = pattern.matcher(pageHtml);
        if (matcher.find()) {
            int start = matcher.end();
            int end = StringUtils.findMatchingQuote(pageHtml, start);
            String userContent = pageHtml.substring(start, end);
            return StringEscapeUtils.unescapeJson(userContent);
        } else {
            throw new DataRetrievalException("Failed to find user data in HTML");
        }
    }
}
