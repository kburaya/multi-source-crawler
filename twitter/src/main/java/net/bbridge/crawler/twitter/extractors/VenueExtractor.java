package net.bbridge.crawler.twitter.extractors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import net.bbridge.crawler.twitter.entities.Tweet;
import net.bbridge.crawler.utils.DataBaseAdapter;
import net.bbridge.crawler.utils.Utils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by kseniya on 20/12/16.
 */
public class VenueExtractor {

    private static final Logger log = Logger.getLogger(VenueExtractor.class.getName());
    private static final HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

    private DataBaseAdapter dataBaseAdapter = new DataBaseAdapter();

    public void onTweetReceived(Tweet tweet) {
        String resp;
        List<String> urls = Utils.extractUrls(tweet.getText());
        for (String url : urls) {
            try {
                HttpGet httpGet = new HttpGet(url);
                resp = EntityUtils.toString(httpClient.execute(httpGet).getEntity());
                DBObject checkin = null;
                if (resp.contains("SwarmCheckinDetail.init")) {
                    checkin = swarmVenueInformationExtraction(resp);
                } else if (resp.contains("VenueDetailPage.init")) {
                    checkin = extractFoursquareVenueInfo(resp);
                }
                if (checkin != null) {
                    checkin.put("userName", tweet.getUsername());
                    checkin.put("date", tweet.getDate());
                    dataBaseAdapter.saveObjectToDataBase((BasicDBObject) checkin, "checkins");
                }
            } catch (Exception e) {
                log.warn(String.format("Can't execute check-in url [%s]", url), e);
            }
        }
    }

    private DBObject extractFoursquareVenueInfo(String foursquareResponse) {
        //when crawling 4sq checkings we can't see users ids/names, use entities id instead
        String jsonString = foursquareResponse.substring(foursquareResponse.indexOf(".VenueDetailPage.init(") + 22, foursquareResponse.indexOf("});});</script>") + 1);
        JSONObject json = new JSONObject(jsonString);
        String checkingInfo = json.getJSONObject("venue").getJSONObject("location").toString();
        return (DBObject) JSON.parse(checkingInfo);
    }

    private DBObject swarmVenueInformationExtraction(String foursquareResponse) {
        String jsonString = foursquareResponse.substring(foursquareResponse.indexOf("checkin: ") + 9, foursquareResponse.indexOf("},venue") + 1);
        JSONObject json = new JSONObject(jsonString);

        String checkingInfo = json.getJSONObject("venue").getJSONObject("location").toString();
        return (DBObject) JSON.parse(checkingInfo);
    }
}
