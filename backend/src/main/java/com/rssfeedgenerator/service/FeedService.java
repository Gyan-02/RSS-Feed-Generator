package com.rssfeedgenerator.service;

import com.rssfeedgenerator.dto.FeedItemRequest;
import com.rssfeedgenerator.dto.FeedRequest;
import com.rssfeedgenerator.exception.FeedNotFoundException;
import com.rssfeedgenerator.exception.FeedValidationException;
import com.rssfeedgenerator.model.Feed;
import com.rssfeedgenerator.model.FeedItem;
import com.rssfeedgenerator.repository.FeedRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final FeedRepository feedRepository;

    public FeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Transactional(readOnly = true)
    public List<Feed> findAllFeeds() {
        return feedRepository.findAllWithItems();
    }

    @Transactional(readOnly = true)
    public Feed findFeed(Long id) {
        return feedRepository.findByIdWithItems(id)
                .orElseThrow(() -> new FeedNotFoundException(id));
    }

    @Transactional
    public Feed createFeed(FeedRequest request) {
        validateRequest(request);
        Feed feed = new Feed();
        applyRequest(feed, request);
        return feedRepository.save(feed);
    }

    @Transactional
    public Feed updateFeed(Long id, FeedRequest request) {
        validateRequest(request);
        Feed feed = findFeed(id);
        applyRequest(feed, request);
        return feedRepository.save(feed);
    }

    @Transactional
    public void deleteFeed(Long id) {
        Feed feed = findFeed(id);
        feedRepository.delete(feed);
    }

    @Transactional(readOnly = true)
    public String buildRssXml(Long id) {
        Feed feed = findFeed(id);
        StringBuilder rss = new StringBuilder();
        rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        rss.append("<rss version=\"2.0\">\n");
        rss.append("    <channel>\n");
        appendTag(rss, "        ", "title", feed.getTitle());
        appendTag(rss, "        ", "link", feed.getWebsite());
        appendTag(rss, "        ", "description", feed.getDescription());
        appendTag(rss, "        ", "author", feed.getAuthor());
        appendTag(rss, "        ", "category", feed.getCategory());

        for (FeedItem item : feed.getItems()) {
            rss.append("        <item>\n");
            appendTag(rss, "            ", "title", item.getTitle());
            appendTag(rss, "            ", "link", item.getUrl());
            appendTag(rss, "            ", "description", item.getDescription());
            rss.append("        </item>\n");
        }

        rss.append("    </channel>\n");
        rss.append("</rss>");
        return rss.toString();
    }

    private void applyRequest(Feed feed, FeedRequest request) {
        feed.setTitle(clean(request.getTitle()));
        feed.setWebsite(clean(request.getWebsite()));
        feed.setDescription(clean(request.getDescription()));
        feed.setAuthor(clean(request.getAuthor()));
        feed.setCategory(clean(request.getCategory()));

        List<FeedItem> items = request.getItems().stream()
                .map(this::toFeedItem)
                .toList();
        feed.setItems(items);
    }

    private FeedItem toFeedItem(FeedItemRequest request) {
        FeedItem item = new FeedItem();
        item.setTitle(clean(request.getTitle()));
        item.setUrl(clean(request.getUrl()));
        item.setDescription(clean(request.getDescription()));
        return item;
    }

    private void validateRequest(FeedRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new FeedValidationException(List.of("Request body is required."));
        }

        require(errors, request.getTitle(), "Feed title is required.");
        require(errors, request.getWebsite(), "Website URL is required.");
        require(errors, request.getDescription(), "Feed description is required.");
        require(errors, request.getAuthor(), "Author name is required.");
        require(errors, request.getCategory(), "Category is required.");

        if (!isHttpUrl(request.getWebsite())) {
            errors.add("Website URL must start with http:// or https://.");
        }

        if (request.getItems().isEmpty()) {
            errors.add("Add at least one feed item before saving a feed.");
        }

        for (int i = 0; i < request.getItems().size(); i++) {
            FeedItemRequest item = request.getItems().get(i);
            int itemNumber = i + 1;
            require(errors, item.getTitle(), "Article " + itemNumber + " title is required.");
            require(errors, item.getUrl(), "Article " + itemNumber + " URL is required.");
            require(errors, item.getDescription(), "Article " + itemNumber + " description is required.");
            if (!isHttpUrl(item.getUrl())) {
                errors.add("Article " + itemNumber + " URL must start with http:// or https://.");
            }
        }

        if (!errors.isEmpty()) {
            throw new FeedValidationException(errors);
        }
    }

    private void require(List<String> errors, String value, String message) {
        if (clean(value).isEmpty()) {
            errors.add(message);
        }
    }

    private boolean isHttpUrl(String value) {
        String cleaned = clean(value).toLowerCase();
        return cleaned.startsWith("http://") || cleaned.startsWith("https://");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void appendTag(StringBuilder rss, String indent, String name, String value) {
        rss.append(indent)
                .append("<")
                .append(name)
                .append(">")
                .append(escapeXml(value))
                .append("</")
                .append(name)
                .append(">\n");
    }

    private String escapeXml(String value) {
        return clean(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
