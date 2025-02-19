package com.yago.chatServer.repository;

import com.yago.chatServer.model.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPasswordRepository extends JpaRepository<UserPassword, Long> {
    UserPassword findByUserId(Long userId);

    UserPassword findByUserUsername(String username);
}
