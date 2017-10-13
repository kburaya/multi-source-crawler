package net.bbridge.crawler.facebook.entities;

import lombok.Data;

import java.util.List;

@Data
public class Post {
    private final String text;
    private final List<String> images;
}
