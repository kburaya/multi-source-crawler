package net.bbridge.crawler.twitter.listeners;

/**
 * Created by kseniya on 23/10/2016.
 */
public interface CrawlerListener<T> {

    void onMessageReceived(T message);
}
