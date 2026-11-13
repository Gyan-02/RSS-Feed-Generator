package com.rssfeedgenerator.repository;

import com.rssfeedgenerator.model.FeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
}
