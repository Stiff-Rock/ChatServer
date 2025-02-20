package com.yago.chatServer.repository;

import com.yago.chatServer.model.BaseChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaseChatRepository extends JpaRepository<BaseChat, Long> {
    List<BaseChat> findByParticipants_Id(Long userId);
}
