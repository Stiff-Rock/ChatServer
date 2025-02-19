package com.yago.chatServer.service;

import com.yago.chatServer.dto.CreateChatDTO;
import com.yago.chatServer.model.Chat;
import com.yago.chatServer.repository.ChatRepository;
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
        System.out.println("CREATING CHAT: " + chat);
        chatRepository.save(chat);
        return chat;
    }
}
