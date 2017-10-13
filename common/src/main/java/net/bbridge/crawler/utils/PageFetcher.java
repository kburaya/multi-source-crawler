package net.bbridge.crawler.utils;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Random;

@RequiredArgsConstructor
public class PageFetcher {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:48.0) Gecko/20100101 Firefox/48.0";
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    public static final int DEFAULT_WAIT_TIME = 0;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;

    private final String cookie;
    private final int waitTime;
    private final int socketTimeout;

    public PageFetcher(String cookie, int waitTime) {
        this(cookie, waitTime, DEFAULT_SOCKET_TIMEOUT);
    }

    public PageFetcher() {
        this(null, DEFAULT_WAIT_TIME, DEFAULT_SOCKET_TIMEOUT);
    }

    public Connection.Response requestPage(String url) throws IOException {
        try {
            Connection connection = Jsoup.connect(url)
                    .timeout(socketTimeout)
                    .userAgent(USER_AGENT)
                    .ignoreContentType(true);
            if (cookie != null) {
                connection.header("Cookie", cookie);
            }
            return connection.execute();
        } finally {
            if (waitTime != 0) {
                try {
                    // wait [time/2 ... 3*time/2]
                    Thread.sleep(waitTime / 2 + RANDOM.nextInt(waitTime));
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    public Document getPageDocument(String url) throws IOException {
        return requestPage(url).parse();
    }

    public String getPageBody(String url) throws IOException {
        return requestPage(url).body();
    }
}
