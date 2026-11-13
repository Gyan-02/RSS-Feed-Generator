package com.rssfeedgenerator.userservice.repository;

import com.rssfeedgenerator.userservice.model.SignInEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignInEventRepository extends JpaRepository<SignInEvent, Long> {

    List<SignInEvent> findByUserIdOrderBySignedInAtDesc(Long userId);
}
