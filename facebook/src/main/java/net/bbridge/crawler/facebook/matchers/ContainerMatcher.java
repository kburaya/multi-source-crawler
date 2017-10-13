package net.bbridge.crawler.facebook.matchers;

import net.bbridge.crawler.exception.DataRetrievalException;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainerMatcher {

    private Pattern pattern;

    public ContainerMatcher(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public String getContainerData(Document pageDocument) throws DataRetrievalException {
        String pageHtml = pageDocument.toString();
        Matcher matcher = pattern.matcher(pageHtml);
        if (matcher.find()) {
            String containerId = matcher.group(1);
            String commentSelector = String.format("code[id=%s]", containerId);
            Element codeElement = pageDocument.select(commentSelector).get(0);
            Comment comment = (Comment) codeElement.childNode(0);
            return comment.getData();
        } else {
            throw new DataRetrievalException("Could not find container data");
        }
    }
}
