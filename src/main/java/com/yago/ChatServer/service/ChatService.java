package com.yago.ChatServer.service;

import com.yago.ChatServer.dto.CreateChatDTO;
import com.yago.ChatServer.model.Chat;
import com.yago.ChatServer.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat createChat(CreateChatDTO createChatDTO) {
        Chat chat = new Chat(createChatDTO.getChatName(), createChatDTO.isGroupChat(), createChatDTO.getParticipants());
        chatRepository.save(chat);
        return chat;
    }
}
