package com.rssfeedgenerator.support;

import com.rssfeedgenerator.model.Feed;
import com.rssfeedgenerator.repository.FeedRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InMemoryFeedRepository {
    private final List<Feed> feeds = new ArrayList<>();
    private long nextId = 1;

    private final FeedRepository repository = (FeedRepository) Proxy.newProxyInstance(
            FeedRepository.class.getClassLoader(), new Class<?>[]{FeedRepository.class}, handler());

    public FeedRepository repository() { return repository; }

    private InvocationHandler handler() {
        return (proxy, method, args) -> switch (method.getName()) {
            case "findAllWithItems" -> new ArrayList<>(feeds);
            case "findByIdWithItems" -> feeds.stream().filter(feed -> feed.getId().equals(args[0])).findFirst();
            case "save" -> save((Feed) args[0]);
            case "delete" -> { feeds.remove(args[0]); yield null; }
            case "toString" -> "InMemoryFeedRepository";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    private Feed save(Feed feed) {
        if (feed.getId() == null) {
            set(feed, "id", nextId++);
            feeds.add(feed);
        }
        return feed;
    }

    private static void set(Object instance, String fieldName, Object value) {
        try {
            var field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
