package com.yago.chatServer.repository;

import com.yago.chatServer.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
}
