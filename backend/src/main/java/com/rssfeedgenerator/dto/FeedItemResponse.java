package com.rssfeedgenerator.dto;

import com.rssfeedgenerator.model.FeedItem;

public class FeedItemResponse {

    private Long id;
    private String title;
    private String url;
    private String description;

    public static FeedItemResponse fromEntity(FeedItem item) {
        FeedItemResponse response = new FeedItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setUrl(item.getUrl());
        response.setDescription(item.getDescription());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
