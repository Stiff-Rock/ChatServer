package com.yago.chatServer.repository;

import com.yago.chatServer.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findByParticipants_Id(Long userId);
}
