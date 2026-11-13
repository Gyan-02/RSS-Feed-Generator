package com.rssfeedgenerator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rssfeedgenerator.exception.GlobalExceptionHandler;
import com.rssfeedgenerator.service.FeedService;
import com.rssfeedgenerator.support.InMemoryFeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedControllerMockMvcTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryFeedRepository();
        var controller = new FeedController(new FeedService(repository.repository()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedLocationAndSavedDetails() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/feeds").contentType(MediaType.APPLICATION_JSON).content(validFeedJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tech News Daily"))
                .andExpect(jsonPath("$.items[0].title").value("Spring Boot Integration"))
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        long id = response.get("id").asLong();
        assertThat(result.getResponse().getHeader("Location")).endsWith("/api/feeds/" + id);
    }

    @Test
    void listReturnsAllCreatedFeeds() throws Exception {
        createFeed(validFeedJson());
        createFeed(validFeedJson().replace("Tech News Daily", "Engineering Weekly"));

        mockMvc.perform(get("/api/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Tech News Daily"))
                .andExpect(jsonPath("$[1].title").value("Engineering Weekly"));
    }

    @Test
    void getReturnsOneSavedFeed() throws Exception {
        long id = createFeed(validFeedJson());

        mockMvc.perform(get("/api/feeds/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Tech News Daily"));
    }

    @Test
    void updatePersistsChangedFields() throws Exception {
        long id = createFeed(validFeedJson());
        String updated = validFeedJson().replace("Tech News Daily", "Engineering Weekly");

        mockMvc.perform(put("/api/feeds/{id}", id).contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Engineering Weekly"));
        mockMvc.perform(get("/api/feeds/{id}", id)).andExpect(jsonPath("$.title").value("Engineering Weekly"));
    }

    @Test
    void deleteRemovesFeedAndReturnsNoContent() throws Exception {
        long id = createFeed(validFeedJson());

        mockMvc.perform(delete("/api/feeds/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/feeds/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidFeedReturnsBadRequestWithValidationMessages() throws Exception {
        String invalid = validFeedJson()
                .replace("https://example.com", "example.com")
                .replace("https://example.com/spring-boot", "article");

        mockMvc.perform(post("/api/feeds").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2));
    }

    @Test
    void missingFeedAndDeleteReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/feeds/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Feed not found"));
        mockMvc.perform(delete("/api/feeds/999")).andExpect(status().isNotFound());
    }

    @Test
    void rssEndpointReturnsXmlContentTypeAndEscapedContent() throws Exception {
        long id = createFeed(validFeedJson().replace("Tech News Daily", "Tech & News"));

        mockMvc.perform(get("/api/feeds/{id}/rss", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/rss+xml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<title>Tech &amp; News</title>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<rss version=\"2.0\">")));
    }

    private long createFeed(String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/feeds").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String validFeedJson() {
        return """
                {
                  "title": "Tech News Daily",
                  "website": "https://example.com",
                  "description": "Latest technology updates.",
                  "author": "Gyan Gaurav",
                  "category": "Technology",
                  "items": [
                    {
                      "title": "Spring Boot Integration",
                      "url": "https://example.com/spring-boot",
                      "description": "Connecting the RSS frontend to a Spring Boot backend."
                    }
                  ]
                }
                """;
    }
}
