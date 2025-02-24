package com.yago.chatServer.websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.Message;
import com.yago.chatServer.model.User;
import com.yago.chatServer.model.WebSocketAction;
import com.yago.chatServer.repository.GroupChatRepository;
import com.yago.chatServer.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;

import static com.yago.chatServer.model.WebSocketAction.USER_CONNECTED_TO_GROUP_CHAT;
import static com.yago.chatServer.model.WebSocketAction.USER_DISCONNECTED_FROM_GROUP_CHAT;
import static com.yago.chatServer.websocket.WebSocketMsgManager.msgToJson;

public class GroupChatWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<User, WebSocketSession> userSessions = new HashMap<>();
    private final HashMap<WebSocketSession, User> sessionUsers = new HashMap<>();

    private GroupChat groupChat;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        if (session.getUri() == null) {
            System.err.println("GroupChatWebSocket Error obtaining session URI");
            return;
        }

        String path = session.getUri().getPath();
        String[] pathSegments = path.split("/");
        String groupId = pathSegments[pathSegments.length - 1];
        if (groupId == null || groupId.isEmpty()) {
            System.err.println("GroupChatWebSocket Error: group id is null or empty");
            return;
        }
        groupChat = groupChatRepository.findById(Long.valueOf(groupId)).orElseThrow(() -> new EntityNotFoundException("GroupChat not found with id " + groupId));

        String query = session.getUri().getQuery();
        if (query == null) {
            System.err.println("GroupChatWebSocket Error: query is null");
            return;
        }

        if (!query.startsWith("username=")) {
            System.err.println("GroupChatWebSocket Error: query does not contain username information");
            return;
        }

        String username = URLDecoder.decode(query.substring("username=".length()), StandardCharsets.UTF_8);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            System.err.println("GroupChatWebSocket Error: could not find user with username: " + username);
            return;
        }

        if (!userSessions.containsKey(user) && !sessionUsers.containsValue(user)) {
            userSessions.put(user, session);
            sessionUsers.put(session, user);
            System.out.println("GroupChatWebSocket connection established with user <" + username + ">");
            broadcastUserStatusChange(user.getUsername(), USER_CONNECTED_TO_GROUP_CHAT, session);
        } else {
            System.err.println("GroupChatWebSocketError: User with username <" + username + "> already connected");
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        User user = sessionUsers.get(session);
        if (user != null) {
            userSessions.remove(user);
            sessionUsers.remove(session);
            System.out.println("Connection closed for user <" + user.getUsername() + ">");
            broadcastUserStatusChange(user.getUsername(), USER_DISCONNECTED_FROM_GROUP_CHAT, session);
        } else {
            System.err.println("Error: Could not find user for the disconnected session");
        }
    }

    private void broadcastUserStatusChange(String username, WebSocketAction action, WebSocketSession session) {
        String msgText = username + " has ";

        switch (action) {
            case USER_CONNECTED_TO_GROUP_CHAT -> msgText += "connected";
            case USER_DISCONNECTED_FROM_GROUP_CHAT -> msgText += "disconnected";
        }

        User sys = userRepository.findByUsername("SYSTEM");
        Message msg = new Message(LocalDateTime.now(), msgText, groupChat, sys);

        ObjectNode jsonMessageNode = msgToJson(action, msg);
        if (jsonMessageNode == null) return;

        for (WebSocketSession targetSession : userSessions.values()) {
            if (!targetSession.equals(session)) try {
                targetSession.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting notification to user <" + sessionUsers.get(targetSession).getUsername() + ">: " + e.getMessage());
            }
        }
    }
}
