package net.bbridge.crawler.facebook;

import lombok.extern.slf4j.Slf4j;
import net.bbridge.crawler.facebook.entities.User;
import net.bbridge.crawler.utils.Config;
import net.bbridge.crawler.utils.PageFetcher;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Scanner;

@Slf4j
public class Main {

    private static final String COOKIE_PROPERTY = "http.cookie";
    private static final String WAIT_TIME_PROPERTY = "http.wait_time";
    private static final String POSTS_LIMIT_PROPERTY = "facebook.posts.limit";

    @Option(name = "-f")
    public File file;

    public static void main(String... args) throws Exception {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            main.doMain();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
        }
    }

    private void doMain() throws Exception {
        Config config = Config.getInstance();
        Properties properties = config.load();
        MongoUsersDAO dao = new MongoUsersDAO(properties);
        PageFetcher pageFetcher = createPageFetcher(config);
        UserSearcher userSearcher = new UserSearcher(pageFetcher);
        Crawler crawler = new Crawler(pageFetcher, userSearcher);
        crawl(config, dao, crawler);
    }

    private PageFetcher createPageFetcher(Config config) throws Exception {
        String facebookCookie = config.getProperty(COOKIE_PROPERTY);
        int waitTime = Integer.parseInt(config.getProperty(WAIT_TIME_PROPERTY));
        return new PageFetcher(facebookCookie, waitTime);
    }

    private void crawl(Config config, MongoUsersDAO dao, Crawler crawler) throws FileNotFoundException {
        int postLimit = Integer.parseInt(config.getProperty(POSTS_LIMIT_PROPERTY));
        Scanner scanner;
        if (file != null) {
            scanner = new Scanner(new FileInputStream(file));
        } else {
            scanner = new Scanner(System.in);
        }
        int processed = 0;
        while (scanner.hasNext()) {
            User user;
            if (scanner.hasNextLong()) {
                long userId = scanner.nextLong();
                user = crawler.retrieve(userId, postLimit);
            } else {
                String email = scanner.next();
                user = crawler.retrieve(email, postLimit);
            }
            log.info("Processed user #{}", ++processed);
            if (user != null) {
                dao.insert(user);
            }
        }
    }
}
