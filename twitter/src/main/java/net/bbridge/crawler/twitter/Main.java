package net.bbridge.crawler.twitter;

import com.mongodb.DBObject;
import net.bbridge.crawler.utils.DataBaseAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kseniya on 24/10/2016.
 */
public class Main {

    public static void main(String[] args) {
        TwitterCrawler crawler = new TwitterCrawler();
        DataBaseAdapter dataBaseAdapter = new DataBaseAdapter();
        List<DBObject> users = dataBaseAdapter.getObjectsFromCollection("users");
        List<String> ids = users.stream()
                .map(user -> String.valueOf(user.get("_id")))
                .collect(Collectors.toList());
        crawler.start(ids);
    }
}
