package net.bbridge.crawler.facebook.matchers;

import net.bbridge.crawler.facebook.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FacebookPostsFetcher {

    public List<Post> getPosts(Document document) throws Exception {
        String userData = getUserData(document);
        return parsePostsFromUserData(userData);
    }

    protected abstract String getUserData(Document document) throws Exception;

    private List<Post> parsePostsFromUserData(String userData) {
        List<Post> posts = new ArrayList<>();
        Document userDataDocument = Jsoup.parse(userData);
        Elements contentWrappers = userDataDocument.select(".userContentWrapper");
        for (Element contentWrapper : contentWrappers) {
            StringBuilder messageBuilder = new StringBuilder();
            List<String> imageLinks = new ArrayList<>();
            Elements contents = contentWrapper.select(".userContent, .userContent + div");
            for (Element content : contents) {
                Elements paragraphs = content.select("p");
                for (Element p : paragraphs) {
                    if (messageBuilder.length() != 0) {
                        messageBuilder.append('\n');
                    }
                    messageBuilder.append(getText(p));
                }
                Elements images = content.select("img");
                images.stream().filter(img -> !img.attr("width").isEmpty()).forEach(img -> {
                    String rawSrc = img.attr("src");
                    String imageUrl = getImageURL(rawSrc);
                    if (imageUrl != null) {
                        imageLinks.add(imageUrl);
                    }
                });
            }
            if (messageBuilder.length() > 0 || !imageLinks.isEmpty()) {
                Post post = new Post(messageBuilder.toString(), imageLinks);
                posts.add(post);
            }
        }
        return posts;
    }

    private String getImageURL(String rawSrc) {
        String src = rawSrc.replace("&amp;", "&");
        String section = getDomainSection(src);
        if (section == null) {
            return src;
        }
        switch (section) {
            case "static":
                return null;
            case "external":
                return findExternalUrl(src);
            default:
                return src;
        }
    }

    private String getDomainSection(String url) {
        Pattern pattern = Pattern.compile("https?://(\\w+)[a-z0-9-]*\\..*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private String findExternalUrl(final String url) {
        final String URL_PREFIX = "url=";
        int start = url.indexOf(URL_PREFIX);
        if (start == -1) {
            return null;
        }
        start += URL_PREFIX.length();
        int end = url.indexOf('&', start);
        if (end == -1) {
            end = url.length();
        }
        String externalUrl = url.substring(start, end);
        try {
            return URLDecoder.decode(externalUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getText(Element element) {
        element.select(".text_exposed_hide").forEach(Element::remove);
        return element.text();
    }
}
