package com.yago.ChatServer.repository;

import com.yago.ChatServer.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByParticipants_Id(Long userId);
}
