import com.mongodb.DBObject;
import net.bbridge.crawler.Crawler;
import net.bbridge.crawler.instagram.InstagramCrawler;
import net.bbridge.crawler.utils.DataBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Crawler crawler = new InstagramCrawler();
        DataBaseAdapter adapter = new DataBaseAdapter();

        List<DBObject> users = adapter.getObjectsFromCollection("test");
        List<String> userNames = new ArrayList<>();
        for (DBObject user: users) {
            userNames.add((String) user.get("instagram"));
        }
        crawler.start(userNames);
    }
}
