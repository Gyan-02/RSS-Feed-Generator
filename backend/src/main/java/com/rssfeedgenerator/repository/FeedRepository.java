package com.rssfeedgenerator.repository;

import com.rssfeedgenerator.model.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("select distinct f from Feed f left join fetch f.items order by f.createdAt desc")
    List<Feed> findAllWithItems();

    @Query("select f from Feed f left join fetch f.items where f.id = :id")
    Optional<Feed> findByIdWithItems(Long id);
}
