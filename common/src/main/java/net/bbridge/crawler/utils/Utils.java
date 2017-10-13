package net.bbridge.crawler.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kseniya on 20/12/16.
 */
public class Utils {

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }
        return containedUrls;
    }
}
