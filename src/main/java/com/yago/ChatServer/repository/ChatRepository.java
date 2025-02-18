package com.yago.ChatServer.repository;

import com.yago.ChatServer.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {

}
