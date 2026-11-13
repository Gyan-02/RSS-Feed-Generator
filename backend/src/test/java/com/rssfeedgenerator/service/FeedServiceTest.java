package com.rssfeedgenerator.service;

import com.rssfeedgenerator.dto.FeedItemRequest;
import com.rssfeedgenerator.dto.FeedRequest;
import com.rssfeedgenerator.exception.FeedNotFoundException;
import com.rssfeedgenerator.exception.FeedValidationException;
import com.rssfeedgenerator.support.InMemoryFeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedServiceTest {
    private InMemoryFeedRepository repository;
    private FeedService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFeedRepository();
        service = new FeedService(repository.repository());
    }

    @Test
    void createFeedTrimsFieldsAndAssociatesItems() {
        var feed = service.createFeed(request("  Tech News  ", " https://example.com ", List.of(item(" Story ", "https://example.com/a", " Description "))));

        assertThat(feed.getId()).isEqualTo(1L);
        assertThat(feed.getTitle()).isEqualTo("Tech News");
        assertThat(feed.getWebsite()).isEqualTo("https://example.com");
        assertThat(feed.getItems()).hasSize(1);
        assertThat(feed.getItems().get(0).getTitle()).isEqualTo("Story");
        assertThat(feed.getItems().get(0).getFeed()).isSameAs(feed);
    }

    @Test
    void createFeedRejectsNullRequest() {
        assertThatThrownBy(() -> service.createFeed(null))
                .isInstanceOf(FeedValidationException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    void createFeedRejectsBlankRequiredFieldsAndMissingItems() {
        var invalid = new FeedRequest();
        invalid.setTitle(" ");
        invalid.setWebsite(" ");
        invalid.setDescription("");
        invalid.setAuthor(null);
        invalid.setCategory("\t");

        assertThatThrownBy(() -> service.createFeed(invalid))
                .isInstanceOf(FeedValidationException.class)
                .hasMessageContaining("Feed title is required")
                .hasMessageContaining("Website URL is required")
                .hasMessageContaining("Feed description is required")
                .hasMessageContaining("Author name is required")
                .hasMessageContaining("Category is required")
                .hasMessageContaining("at least one feed item");
    }

    @Test
    void createFeedRejectsInvalidWebsiteAndArticleUrlsTogether() {
        var invalid = request("Title", "example.com", List.of(item("Story", "article", "Description")));

        assertThatThrownBy(() -> service.createFeed(invalid))
                .isInstanceOf(FeedValidationException.class)
                .hasMessageContaining("Website URL must start with http:// or https://")
                .hasMessageContaining("Article 1 URL must start with http:// or https://");
    }

    @Test
    void createFeedCollectsMissingFieldsFromEveryItem() {
        var invalid = request("Title", "https://example.com", List.of(
                item("", "https://example.com/one", "Description"),
                item("Second", "", "")));

        assertThatThrownBy(() -> service.createFeed(invalid))
                .isInstanceOf(FeedValidationException.class)
                .hasMessageContaining("Article 1 title is required")
                .hasMessageContaining("Article 2 URL is required")
                .hasMessageContaining("Article 2 description is required");
    }

    @Test
    void findAllReturnsSavedFeeds() {
        service.createFeed(validRequest());
        service.createFeed(request("Second", "http://example.org", List.of(item("Entry", "http://example.org/item", "Text"))));

        assertThat(service.findAllFeeds()).hasSize(2);
    }

    @Test
    void findFeedThrowsForUnknownId() {
        assertThatThrownBy(() -> service.findFeed(404L))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void updateFeedReplacesMetadataAndItemCollection() {
        var saved = service.createFeed(validRequest());

        var updated = service.updateFeed(saved.getId(), request("Updated", "https://new.example", List.of(item("New story", "https://new.example/post", "New text"))));

        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getWebsite()).isEqualTo("https://new.example");
        assertThat(updated.getItems()).extracting("title").containsExactly("New story");
        assertThat(updated.getItems().get(0).getFeed()).isSameAs(updated);
    }

    @Test
    void updateFeedRejectsInvalidRequestBeforeLookingUpFeed() {
        var invalid = request("", "invalid", List.of());

        assertThatThrownBy(() -> service.updateFeed(999L, invalid))
                .isInstanceOf(FeedValidationException.class)
                .hasMessageContaining("Feed title is required");
    }

    @Test
    void deleteFeedRemovesSavedFeed() {
        var saved = service.createFeed(validRequest());

        service.deleteFeed(saved.getId());

        assertThat(service.findAllFeeds()).isEmpty();
        assertThatThrownBy(() -> service.findFeed(saved.getId())).isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    void buildRssXmlIncludesFeedAndArticleFields() {
        var saved = service.createFeed(validRequest());

        String xml = service.buildRssXml(saved.getId());

        assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(xml).contains("<rss version=\"2.0\">")
                .contains("<title>Tech News Daily</title>")
                .contains("<link>https://example.com</link>")
                .contains("<title>Spring Boot Integration</title>")
                .contains("</channel>");
    }

    @Test
    void buildRssXmlEscapesAllSpecialCharacters() {
        var saved = service.createFeed(request("R&D <News>", "https://example.com", List.of(
                item("A & B", "https://example.com/a?x=1&y=2", "Quotes \" and apostrophe ' and <tag>"))));

        String xml = service.buildRssXml(saved.getId());

        assertThat(xml).contains("R&amp;D &lt;News&gt;")
                .contains("A &amp; B")
                .contains("x=1&amp;y=2")
                .contains("Quotes &quot; and apostrophe &apos; and &lt;tag&gt;");
    }

    @Test
    void rssRequestForUnknownFeedThrowsNotFound() {
        assertThatThrownBy(() -> service.buildRssXml(404L)).isInstanceOf(FeedNotFoundException.class);
    }

    private static FeedRequest validRequest() {
        return request("Tech News Daily", "https://example.com", List.of(item(
                "Spring Boot Integration", "https://example.com/spring-boot", "Connecting the RSS frontend to a Spring Boot backend.")));
    }

    private static FeedRequest request(String title, String website, List<FeedItemRequest> items) {
        var request = new FeedRequest();
        request.setTitle(title);
        request.setWebsite(website);
        request.setDescription("Latest technology updates.");
        request.setAuthor("Gyan Gaurav");
        request.setCategory("Technology");
        request.setItems(items);
        return request;
    }

    private static FeedItemRequest item(String title, String url, String description) {
        var item = new FeedItemRequest();
        item.setTitle(title);
        item.setUrl(url);
        item.setDescription(description);
        return item;
    }
}
