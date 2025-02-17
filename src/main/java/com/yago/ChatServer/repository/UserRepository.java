package com.yago.ChatServer.repository;

import com.yago.ChatServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);

    User findById(Long id);

    boolean existsByUsername(String username);
}
