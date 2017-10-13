package net.bbridge.crawler.twitter.entities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kseniya on 23/10/2016.
 */
public class TwitterCredentials {

    private static Logger log = Logger.getLogger(TwitterCredentials.class.getName());
    private long startTime;

    private String consumerKey;
    private String consumerSecret;
    private String userAccessToken;
    private String userAccessSecret;
    private static int credentialsNumber;
    private static int currentCredentials;
    private Properties properties;

    public TwitterCredentials() {
        log.setLevel(Level.INFO);
        properties = new Properties();
        InputStream input = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream("credentials.properties");
            properties.load(input);

            credentialsNumber = Integer.valueOf(properties.getProperty("credentialsNumber"));
            currentCredentials = 1;
            input.close();

            setCredentialsFromProperties(currentCredentials);
            startTime = System.currentTimeMillis();
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

    private void setCredentialsFromProperties(int id) {
        this.consumerKey = properties.getProperty(String.format("consumerKey%s", String.valueOf(id)));
        this.consumerSecret = properties.getProperty(String.format("consumerSecret%s", String.valueOf(id)));
        this.userAccessToken = properties.getProperty(String.format("userAccessToken%s", String.valueOf(id)));
        this.userAccessSecret = properties.getProperty(String.format("userAccessSecret%s", String.valueOf(id)));
    }

    public void switchCredentials() {
        if (currentCredentials == credentialsNumber) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long elapsedSeconds = elapsedTime / 1000;
            long elapsedMinutes = elapsedSeconds / 60;
            if (elapsedMinutes <= 15) {
                try {
                    log.info(String.format("There are no credentials without timeout, sleep for %s minutes", 15 - elapsedMinutes + 2));
                    Thread.sleep((15 - elapsedMinutes + 2) * 60 * 1000);
                } catch (InterruptedException e) {
                    log.error("Have an exception while pausing for entities timeout, fatal error", e);
                    return;
                }
            }
            currentCredentials = 1;
            setCredentialsFromProperties(currentCredentials);
            log.info(String.format("Successfully switch credentials to number %s, continue crawling", String.valueOf(currentCredentials)));
            startTime = System.currentTimeMillis();
            return;
        }
        currentCredentials += 1;
        setCredentialsFromProperties(currentCredentials);
        log.info(String.format("Successfully switch credentials to number %s, continue crawling", String.valueOf(currentCredentials)));
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public String getUserAccessSecret() {
        return userAccessSecret;
    }
}
