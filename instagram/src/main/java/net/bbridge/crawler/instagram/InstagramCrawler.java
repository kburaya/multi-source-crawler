package net.bbridge.crawler.instagram;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import net.bbridge.crawler.Crawler;
import net.bbridge.crawler.utils.DataBaseAdapter;
import net.bbridge.crawler.utils.PageFetcher;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by kseniya on 22/12/16.
 */
public class InstagramCrawler implements Crawler {

    private static Logger log = Logger.getLogger(InstagramCrawler.class.getName());

    private final MediaExtractor mediaExtractor;
    private final DataBaseAdapter dataBaseAdapter;
    private final PageFetcher pageFetcher;

    public InstagramCrawler() {
        this.mediaExtractor = new MediaExtractor();
        this.dataBaseAdapter = new DataBaseAdapter();
        this.pageFetcher = new PageFetcher();
    }

    @Override
    public void start(List<String> userNames) {
        userNames.forEach(this::collectInstagramTimeline);
    }

    @Override
    public void stop() {
    }

    private void collectInstagramTimeline(String userName) {
        String max_id = "1";
        Boolean more_available = true;
        JSONObject resp;
        JSONArray medias;
        while (more_available) {
            String timeLineURL = String.format("https://www.instagram.com/%s/media/?max_id=%s", userName, max_id);
            try {
                resp = new JSONObject(pageFetcher.getPageBody(timeLineURL));
            } catch (Exception e) {
                log.warn(String.format("Failed to execute instagram media url [%s], skipping", timeLineURL));
                continue;
            }
            more_available = (Boolean) resp.get("more_available");
            medias = (JSONArray) resp.get("items");
            if (medias.length() == 0) {
                log.warn("Seems to be closed account, can't collect timeline, no media available");
                return;
            }

            for (int i = 0; i < medias.length(); ++i) {
                JSONObject media = (JSONObject) medias.get(i);
                String mediaLink = (String) media.get("link");
                String mediaInfo = mediaExtractor.getJSONFromImagePage(mediaLink);
                try {
                    mediaInfo = mediaInfo.replace("id", "_id");
                } catch (NullPointerException e) {
                    log.warn(String.format("Image with url [%s] hasn't got id field", mediaLink));
                    continue;
                }
                DBObject mediaDBObject = (DBObject) JSON.parse(mediaInfo);
                if (dataBaseAdapter.isObjectExists(new BasicDBObject("_id", mediaDBObject.get("_id")), "media")) {
                    max_id = (String) mediaDBObject.get("_id");
                    continue;
                }
                String locationInfo = null;
                try {
                    locationInfo = mediaExtractor.getLocationIdFromImage(mediaInfo);
                } catch (Exception e) {
                    log.warn("Cannot get location info for media", e);
                }

                if (locationInfo != null) {
                    locationInfo = locationInfo.replace("id", "_id");
                    DBObject locationDBObject = (DBObject) JSON.parse(locationInfo);
                    mediaDBObject.removeField("location");
                    mediaDBObject.put("location", locationDBObject);
                }
                if (!dataBaseAdapter.isObjectExists(new BasicDBObject("_id", mediaDBObject.get("_id")), "media"))
                    dataBaseAdapter.saveObjectToDataBase((BasicDBObject) mediaDBObject, "media");
                else
                    log.info(String.format("Already collected image [%s]", mediaDBObject.get("_id")));
                max_id = (String) mediaDBObject.get("_id");
            }
        }
        log.info(String.format("Collected instagram timeline for user [%s]", userName));
    }
}
