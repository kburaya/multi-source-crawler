package net.bbridge.crawler.twitter.entities;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by kseniya on 01/12/2016.
 */
@Data
public class Tweet {
    private final String id;
    private final long userId;
    private final String username;
    private final String url;
    private final TweetSource source;
    private final String text;
    private final Date date;
    private final int retweets;
    private final int favorites;
    private final List<String> mentions;
    private final List<String> hashtags;
    private final String geo;
}
