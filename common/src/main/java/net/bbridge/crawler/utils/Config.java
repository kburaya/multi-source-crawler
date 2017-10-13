package net.bbridge.crawler.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class Config {

    private static final String CONFIG_FILE = "crawler.properties";

    private static final Config INSTANCE = new Config();

    private Properties properties;

    private Config() {}

    public static Config getInstance() {
        return INSTANCE;
    }

    public Properties load() throws IOException {
        if (properties != null) {
            return properties;
        }
        log.info("Loading properties from file " + CONFIG_FILE);
        this.properties = new Properties();
        this.properties.load(new FileReader(CONFIG_FILE));
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.setProperty(key, value);
        }
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Properties getProperties() {
        return this.properties;
    }
}
