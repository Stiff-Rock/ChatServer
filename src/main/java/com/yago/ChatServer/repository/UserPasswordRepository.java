package com.yago.ChatServer.repository;

import com.yago.ChatServer.model.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPasswordRepository extends JpaRepository<UserPassword, Long> {
    UserPassword findByUserId(Long userId);

    UserPassword findByUserUsername(String username);
}
