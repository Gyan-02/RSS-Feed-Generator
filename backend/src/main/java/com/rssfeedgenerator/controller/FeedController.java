package com.rssfeedgenerator.controller;

import com.rssfeedgenerator.dto.FeedRequest;
import com.rssfeedgenerator.dto.FeedResponse;
import com.rssfeedgenerator.model.Feed;
import com.rssfeedgenerator.service.FeedService;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/feeds")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public List<FeedResponse> getFeeds() {
        return feedService.findAllFeeds().stream()
                .map(FeedResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public FeedResponse getFeed(@PathVariable Long id) {
        return FeedResponse.fromEntity(feedService.findFeed(id));
    }

    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(@RequestBody FeedRequest request) {
        Feed feed = feedService.createFeed(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(feed.getId())
                .toUri();
        return ResponseEntity.created(location).body(FeedResponse.fromEntity(feed));
    }

    @PutMapping("/{id}")
    public FeedResponse updateFeed(@PathVariable Long id, @RequestBody FeedRequest request) {
        return FeedResponse.fromEntity(feedService.updateFeed(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id) {
        feedService.deleteFeed(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/rss", produces = "application/rss+xml")
    public ResponseEntity<String> getRssXml(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/rss+xml;charset=UTF-8"))
                .body(feedService.buildRssXml(id));
    }
}
