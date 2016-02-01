package com.example.siddhant.article_publisher.networkClasses;

/**
 * Created by siddhant on 2/2/16.
 */
public class PublishArticle {
    public int publisher;
    public int category;
    public String title;
    public String content;
    public long timestamp;

    public PublishArticle(int publisher, int category, String title, String content) {
        this.publisher = publisher;
        this.category = category;
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}
