package com.rssfeedgenerator.userservice.support;

import com.rssfeedgenerator.userservice.model.AppUser;
import com.rssfeedgenerator.userservice.model.SignInEvent;
import com.rssfeedgenerator.userservice.repository.SignInEventRepository;
import com.rssfeedgenerator.userservice.repository.UserRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class InMemoryRepositories {
    private final List<AppUser> users = new ArrayList<>();
    private final List<SignInEvent> events = new ArrayList<>();
    private long nextUserId = 1;
    private long nextEventId = 1;

    private final UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> switch (method.getName()) {
        case "existsByEmail" -> users.stream().anyMatch(user -> user.getEmail().equals(args[0]));
        case "existsByEmailAndIdNot" -> users.stream().anyMatch(user -> user.getEmail().equals(args[0]) && !user.getId().equals(args[1]));
        case "findByEmail" -> users.stream().filter(user -> user.getEmail().equals(args[0])).findFirst();
        case "findById" -> users.stream().filter(user -> user.getId().equals(args[0])).findFirst();
        case "findAll" -> new ArrayList<>(users);
        case "save" -> saveUser((AppUser) args[0]);
        case "toString" -> "InMemoryUserRepository";
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == args[0];
        default -> throw new UnsupportedOperationException(method.getName());
    });

    private final SignInEventRepository signInEventRepository = proxy(SignInEventRepository.class, (proxy, method, args) -> switch (method.getName()) {
        case "save" -> saveEvent((SignInEvent) args[0]);
        case "findByUserIdOrderBySignedInAtDesc" -> events.stream()
                .filter(event -> event.getUser().getId().equals(args[0]))
                .sorted(Comparator.comparing(SignInEvent::getSignedInAt).reversed())
                .toList();
        case "toString" -> "InMemorySignInEventRepository";
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == args[0];
        default -> throw new UnsupportedOperationException(method.getName());
    });

    public UserRepository users() { return userRepository; }
    public SignInEventRepository signIns() { return signInEventRepository; }

    private AppUser saveUser(AppUser user) {
        if (user.getId() == null) {
            set(user, "id", nextUserId++);
            set(user, "registeredAt", Instant.now());
            users.add(user);
        }
        return user;
    }

    private SignInEvent saveEvent(SignInEvent event) {
        set(event, "id", nextEventId++);
        set(event, "signedInAt", Instant.now());
        events.add(event);
        return event;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
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
