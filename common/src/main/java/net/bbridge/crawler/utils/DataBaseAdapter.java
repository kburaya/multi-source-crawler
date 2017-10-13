package net.bbridge.crawler.utils;

import com.mongodb.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by kseniya on 29/11/2016.
 */
public class DataBaseAdapter {

    private static Logger log = Logger.getLogger(DataBaseAdapter.class.getName());
    private DB db;

    public DataBaseAdapter() {
        initMongoDB();
    }

    private void initMongoDB() throws MongoException {
        Properties properties = new Properties();
        InputStream input = null;
        log.info("Connecting to Mongo DB");
        try {
            input = getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(input);

            Mongo mongo = new Mongo(properties.getProperty("mongo.host", properties.getProperty("mongo.port")));
            this.db = mongo.getDB(properties.getProperty("mongo.database"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveObjectToDataBase(BasicDBObject basicObj, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        collection.insert(basicObj);
    }

    public boolean isObjectExists(BasicDBObject fields, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        DBCursor result = collection.find(fields);
        return (result.size() > 0);
    }

    public void updateObject(BasicDBObject object, BasicDBObject fields, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        collection.update(object, new BasicDBObject("$set", fields));
    }

    public List<DBObject> getObjectsFromCollection(String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        return collection.find().toArray();
    }
}
