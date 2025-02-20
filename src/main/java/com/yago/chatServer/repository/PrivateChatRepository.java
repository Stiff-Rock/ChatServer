package com.yago.chatServer.repository;

import com.yago.chatServer.model.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {
    PrivateChat findByUniqueHash(String uniqueHash);
}
