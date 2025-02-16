package com.yago.ChatServer;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final HashMap<String, WebSocketSession> userSessions = new HashMap<>();

    // ENVIAR EL HISTORIAL DE MENSAJES
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String query;
        if (session.getUri() != null) {
            query = session.getUri().getQuery();

            String username;
            if (query != null && query.startsWith("username=")) {
                username = query.substring("username=".length());
                username = URLDecoder.decode(username, StandardCharsets.UTF_8);
                if (username != null) userSessions.put(username, session);
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        System.out.println("Closed connection with: " + userSessions.get(session));
    }

    public void broadcastMessageToGroup(String message) {
        System.out.println("MESSAGE TO GROUP: " + message);
    }

    public void broadcastMessageToUser(String message, String recipient) {
        WebSocketSession session = userSessions.get(recipient);

        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("Error broadcasting message \"" + message + "\": " + e.getMessage());
            }
        }
    }
}
