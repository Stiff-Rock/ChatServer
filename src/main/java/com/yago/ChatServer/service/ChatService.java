package com.yago.ChatServer.service;

import com.yago.ChatServer.model.Chat;
import com.yago.ChatServer.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
}
