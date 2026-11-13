package com.rssfeedgenerator.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedRequest {

    private String title;
    private String website;
    private String description;
    private String author;
    private String category;
    private List<FeedItemRequest> items = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<FeedItemRequest> getItems() {
        return items;
    }

    public void setItems(List<FeedItemRequest> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
