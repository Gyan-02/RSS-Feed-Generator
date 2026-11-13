package com.rssfeedgenerator.userservice.repository;

import com.rssfeedgenerator.userservice.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<AppUser> findByEmail(String email);
}
