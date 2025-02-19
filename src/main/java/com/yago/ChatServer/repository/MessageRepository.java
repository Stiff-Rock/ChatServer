package com.yago.ChatServer.repository;

import com.yago.ChatServer.model.Message;
import com.yago.ChatServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
