package com.yago.ChatServer.websocket;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<String, WebSocketSession> userSessions = new HashMap<>();
    private final HashMap<WebSocketSession, String> sessionUsers = new HashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String query;
        if (session.getUri() != null) {
            query = session.getUri().getQuery();

            String username;
            if (query != null && query.startsWith("username=")) {
                username = query.substring("username=".length());
                username = URLDecoder.decode(username, StandardCharsets.UTF_8);
                if (username != null && !userSessions.containsKey(username) && !sessionUsers.containsValue(username)) {
                    userSessions.put(username, session);
                    sessionUsers.put(session, username);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String user = sessionUsers.get(session);
        if (user != null) {
            userSessions.remove(user);
            sessionUsers.remove(session);
        }
    }

    public void broadcastMessageToChatGroup(String message) {
        System.out.println("MESSAGE TO GROUP: " + message);
    }

    public void broadcastMessageToPrivateChat(String message, String recipient) {
        WebSocketSession session = userSessions.get(recipient);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("Error broadcasting message \"" + message + "\": " + e.getMessage());
            }
        }
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(userSessions.keySet());
    }
}
