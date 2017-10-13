package net.bbridge.crawler.instagram;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import net.bbridge.crawler.utils.DataBaseAdapter;
import net.bbridge.crawler.utils.PageFetcher;
import net.bbridge.crawler.utils.Utils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by kseniya on 09/12/2016.
 */
public class MediaExtractor {

    private static Logger log = Logger.getLogger(MediaExtractor.class.getName());

    private final DataBaseAdapter dataBaseAdapter = new DataBaseAdapter();
    private final PageFetcher pageFetcher = new PageFetcher();

    public BasicDBObject onTweetReceived(String message) {
        List<String> urls = Utils.extractUrls(message);
        for (String url : urls) {
            if (url.contains("instagram.com") || url.contains("instagr.am")) {
                String postInfoString = getJSONFromImagePage(url);
                postInfoString = postInfoString.replace("id", "_id");
                DBObject mediaDBObject = (DBObject) JSON.parse(postInfoString);
                if (!dataBaseAdapter.isObjectExists(new BasicDBObject("_id", mediaDBObject.get("_id")), "media"))
                    dataBaseAdapter.saveObjectToDataBase((BasicDBObject) mediaDBObject, "media");
                else
                    log.info(String.format("Image with id [%s] is already in database", mediaDBObject.get("_id")));

                JSONObject postInfoJSON = new JSONObject(postInfoString);
                String instagramUserId = (String) postInfoJSON.getJSONObject("owner").get("_id");
                String instagramUserName = (String) postInfoJSON.getJSONObject("owner").get("username");
                log.info(String.format("Find instagram post for instagram user [%s]", instagramUserName));

                BasicDBObject userInfo = new BasicDBObject();
                userInfo.put("instagramId", instagramUserId);
                userInfo.put("instagramUserName", instagramUserName);
                return userInfo;
            }
        }
        return null;
    }

    public String getJSONFromImagePage(String imageURL) {
        //we need to get answers for 2 links to have location coordinates: link to image/video page, link to location page
        String resp;
        try {
            resp = pageFetcher.getPageBody(imageURL);
        } catch (Exception e) {
            log.warn(String.format("Failed to execute image url [%s]", imageURL), e);
            return null;
        }

        String jsonString = resp.substring(resp.indexOf("._sharedData = ") + 15, resp.indexOf("};</script") + 1);
        JSONObject json = new JSONObject(jsonString);
        return json.getJSONObject("entry_data").getJSONArray("PostPage").getJSONObject(0).getJSONObject("media").toString();
    }

    public String getLocationIdFromImage(String imageJSONString) {
        JSONObject imageJSON = new JSONObject(imageJSONString);
        try {
            String id = (String) imageJSON.getJSONObject("location").get("id");
            Boolean hasPublicPage = (Boolean) imageJSON.getJSONObject("location").get("has_public_page");
            if (!hasPublicPage) {
                log.warn(String.format("Location id [%s] haven't got public page, skipping", id));
                return null;
            }
            String locationURL = String.format("https://www.instagram.com/explore/locations/%s/", id);
            String resp;
            try {
                resp = pageFetcher.getPageBody(locationURL);
            } catch (Exception e) {
                log.warn(String.format("Can't execute location url [%s]", locationURL), e);
                return null;
            }
            String jsonString = resp.substring(resp.indexOf("._sharedData = ") + 15, resp.indexOf("};</script") + 1);
            JSONObject json = new JSONObject(jsonString);
            return json.getJSONObject("entry_data").getJSONArray("LocationsPage").getJSONObject(0).getJSONObject("location").toString();
        } catch (Exception e) {
            log.info("The image doesn't contain location");
        }
        return null;
    }
}
