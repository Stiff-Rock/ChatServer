package com.yago.chatServer.service;

import com.yago.chatServer.dto.GroupChatDTO;
import com.yago.chatServer.dto.PrivateChatDTO;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.GroupChatRepository;
import com.yago.chatServer.repository.PrivateChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.websocket.ChatWebSocketHandler;
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
    private ChatWebSocketHandler chatWebSocketHandler;

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
            System.out.println("RETURNING EXISTENT CHAT");
            return existentChat;
        }

        User user1 = userRepository.findById(userId1).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId1));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId2));

        PrivateChat chat = new PrivateChat(user1, user2);
        System.out.println("CREATING PRIVATE CHAT: " + chat);

        privateChatRepository.save(chat);

        chatWebSocketHandler.broadcastNewChat(chat, userId1);
        return chat;
    }

    public GroupChat createGroupChat(GroupChatDTO gcd) {
        Set<User> users = new HashSet<>();

        for (Long userId : gcd.getParticipants()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            users.add(user);
        }

        GroupChat groupChat = new GroupChat(gcd.getChatName(), users);
        System.out.println("CREATING GROUP CHAT: " + groupChat);

        groupChatRepository.save(groupChat);

        chatWebSocketHandler.broadcastNewChat(groupChat, gcd.getAdminId());
        return groupChat;
    }
}
