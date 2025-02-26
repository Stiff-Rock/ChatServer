package com.yago.chatServer.service;

import com.yago.chatServer.dto.GroupChatDTO;
import com.yago.chatServer.dto.PrivateChatDTO;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.GroupChatRepository;
import com.yago.chatServer.repository.PrivateChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {
    @Autowired
    private PrivateChatRepository privateChatRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppWebSocketHandler appWebSocketHandler;

    public PrivateChat createPrivateChat(PrivateChatDTO pcd) {
        Long userId1 = pcd.getUserId1();
        Long userId2 = pcd.getUserId2();

        // Comprueba si existe ya el chat en la bbdd
        List<Long> ids = new ArrayList<>();
        ids.add(userId1);
        ids.add(userId2);
        Collections.sort(ids);
        String uniqueHash = ids.size() >= 2 ? ids.get(0) + ":" + ids.get(1) : "";
        PrivateChat existentChat = privateChatRepository.findByUniqueHash(uniqueHash);
        if (existentChat != null) {
            return existentChat;
        }

        User user1 = userRepository.findById(userId1).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId1));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId2));

        PrivateChat chat = new PrivateChat(user1, user2);

        privateChatRepository.save(chat);
        //TODO: PUT THE NAME OF THE OPPOSIT USER IN THE ANME OF THE CHAT
        appWebSocketHandler.broadcastNewChat(chat, userId1);
        return chat;
    }

    public GroupChat createGroupChat(GroupChatDTO gcd) {
        Set<User> users = new HashSet<>();

        for (Long userId : gcd.getParticipants()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
            users.add(user);
        }

        Long adminId = gcd.getAdminId();
        User admin = userRepository.findById(adminId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + adminId));

        Set<User> admins = new HashSet<>();
        admins.add(admin);
        GroupChat groupChat = new GroupChat(gcd.getChatName(), users, admins);

        groupChatRepository.save(groupChat);

        appWebSocketHandler.broadcastNewChat(groupChat, gcd.getAdminId());
        return groupChat;
    }
}
