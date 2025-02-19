package com.yago.chatServer.service;

import com.yago.chatServer.dto.CreateChatDTO;
import com.yago.chatServer.model.Chat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.ChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    public Chat createChat(CreateChatDTO cct) {
        System.out.println("ANOTHER LOOK AT CCT: " + cct);
        Set<User> users = new HashSet<>();
        for (Long userId : cct.getParticipants()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            users.add(user);
        }
        Chat chat = new Chat(cct.getChatName(), cct.isGroupChat(), users);
        System.out.println("CREATING CHAT: " + chat);
        chatRepository.save(chat);
        chatWebSocketHandler.broadcastNewChat(chat);
        return chat;
    }
}
