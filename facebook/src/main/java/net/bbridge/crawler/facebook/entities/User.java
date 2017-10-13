package net.bbridge.crawler.facebook.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String email;
    private List<Post> posts;

    public User(long id) {
        this.id = id;
    }

    public User(long id, String username) {
        this.id = id;
        this.username = username;
    }
}
