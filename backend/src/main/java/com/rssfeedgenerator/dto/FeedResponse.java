package com.rssfeedgenerator.dto;

import com.rssfeedgenerator.model.Feed;
import java.time.LocalDateTime;
import java.util.List;

public class FeedResponse {

    private Long id;
    private String title;
    private String website;
    private String description;
    private String author;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedItemResponse> items;

    public static FeedResponse fromEntity(Feed feed) {
        FeedResponse response = new FeedResponse();
        response.setId(feed.getId());
        response.setTitle(feed.getTitle());
        response.setWebsite(feed.getWebsite());
        response.setDescription(feed.getDescription());
        response.setAuthor(feed.getAuthor());
        response.setCategory(feed.getCategory());
        response.setCreatedAt(feed.getCreatedAt());
        response.setUpdatedAt(feed.getUpdatedAt());
        response.setItems(feed.getItems().stream().map(FeedItemResponse::fromEntity).toList());
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FeedItemResponse> getItems() {
        return items;
    }

    public void setItems(List<FeedItemResponse> items) {
        this.items = items;
    }
}
