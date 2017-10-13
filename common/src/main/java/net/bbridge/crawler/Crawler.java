package net.bbridge.crawler;

import java.util.List;

/**
 * Created by kseniya on 23/10/2016.
 */
public interface Crawler {

    void start(List<String> ids);

    void stop();
}
