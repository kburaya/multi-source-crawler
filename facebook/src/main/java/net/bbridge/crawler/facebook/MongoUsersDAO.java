package net.bbridge.crawler.facebook;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import net.bbridge.crawler.facebook.entities.Post;
import net.bbridge.crawler.facebook.entities.User;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class MongoUsersDAO {

    private static final String HOST_PROPERTY = "mongo.host";
    private static final String PORT_PROPERTY = "mongo.port";
    private static final String DATABASE_PROPERTY = "mongo.database";
    private static final String USERNAME_PROPERTY = "mongo.username";
    private static final String PASSWORD_PROPERTY = "mongo.password";
    private static final String COLLECTION_PROPERTY = "mongo.collection";

    private final MongoCollection<Document> collection;

    public MongoUsersDAO(Properties properties) {
        this.collection = getCollection(properties);
        log.debug("Connection to database initialized");
    }

    private MongoCollection<Document> getCollection(Properties properties) {
        String database         = properties.getProperty(DATABASE_PROPERTY);
        String collectionName   = properties.getProperty(COLLECTION_PROPERTY);

        MongoClient mongoClient = getMongoClient(properties);
        MongoDatabase db = mongoClient.getDatabase(database);
        return db.getCollection(collectionName);
    }

    private MongoClient getMongoClient(Properties properties) {
        String host             = properties.getProperty(HOST_PROPERTY);
        int    port             = Integer.parseInt(properties.getProperty(PORT_PROPERTY));
        String database         = properties.getProperty(DATABASE_PROPERTY);
        String username         = properties.getProperty(USERNAME_PROPERTY);
        String password         = properties.getProperty(PASSWORD_PROPERTY);

        if (!username.isEmpty() && !password.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            return new MongoClient(new ServerAddress(host, port), Collections.singletonList(credential));
        } else {
            return new MongoClient(host, port);
        }
    }

    public void insert(User user) {
        Document document = userToDoc(user);
        this.collection.insertOne(document);
    }

    private Document userToDoc(User user) {
        Document userDocument = new Document("userId", user.getId());
        if (user.getUsername() != null) {
            userDocument.put("username", user.getUsername());
        }
        if (user.getEmail() != null) {
            userDocument.put("email", user.getEmail());
        }
        List<Document> postsDBO = user.getPosts().stream().map(this::postToDoc).collect(Collectors.toList());
        userDocument.put("posts", postsDBO);
        return userDocument;
    }

    private Document postToDoc(Post post) {
        Document postDoc = new Document("text", post.getText());
        postDoc.put("images", post.getImages());
        return postDoc;
    }

    public boolean contains(String collection, long userId) {
        return this.collection.find(new Document("userId", userId)).first() != null;
    }
}
