package com.yago.chatServer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yago.chatServer.model.BaseChat;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.Message;
import com.yago.chatServer.model.User;
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

//TODO: CASCADE DELETIONS
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<String, WebSocketSession> userSessions = new HashMap<>();
    private final HashMap<WebSocketSession, String> sessionUsers = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
                    System.out.println("WebSocket connection established with user <" + username + ">");
                    userSessions.put(username, session);
                    sessionUsers.put(session, username);
                } else {
                    System.err.println("Error: Connected user does not provide username in query");
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String user = sessionUsers.get(session);
        System.out.println("Connection closed for user <" + user + ">");
        if (user != null) {
            userSessions.remove(user);
            sessionUsers.remove(session);
        }
    }

    //TODO BORADCSAST MESSAGE DIRECTLY
    public void broadcastMessageToChatGroup(Message message) {
        System.out.println("BROADCASTING MESSAGE TO GROUP");
        GroupChat groupChat = (GroupChat) message.getChat();
        String chatId = String.valueOf(message.getId());
        for (User recipient : groupChat.getParticipants()) {
            String username = recipient.getUsername();
            WebSocketSession session = userSessions.get(username);
            try {
                session.sendMessage(new TextMessage(chatId));
            } catch (IOException e) {
                System.err.println("Error broadcasting message \"" + message + "\": " + e.getMessage());
            }
            System.out.println(" - MESSAGE SENT TO: " + username);
        }
    }

    public void broadcastMessageToPrivateChat(Message message) {
        System.out.println("BROADCASTING MESSAGE TO PRIVATE CHAT");

        System.out.println("SENDER: " + message.getSender().getUsername());

        //TODO REVISE
        User recipient = null;
        for (User user : message.getChat().getParticipants()) {
            if (!user.getUsername().equals(message.getSender().getUsername())) recipient = user;
        }

        if (recipient == null) {
            System.err.println("COULD NOT FIND USER RECIPIENT");
            return;
        }

        System.out.println("RECIPIENT: " + recipient.getUsername());

        WebSocketSession session = userSessions.get(recipient.getUsername());
        if (session != null && session.isOpen()) {
            try {
                System.out.println("WEBSOCKET MESSAGE SENT");
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                System.err.println("Error broadcasting message \"" + message + "\": " + e.getMessage());
            }
        }
    }

    public void broadcastNewChat(BaseChat chat, Long creatorId) {
        long chatId = chat.getId();
        for (User recipient : chat.getParticipants()) {
            if (recipient.getId().equals(creatorId)) continue;

            String username = recipient.getUsername();
            WebSocketSession session = userSessions.get(username);

            if (session == null) {
                System.err.println("Could not broadcast to session of user: " + username + ": Session is null");
                return;
            }

            try {
                ObjectMapper oM = new ObjectMapper();
                ObjectNode json = oM.createObjectNode();
                json.put("action", "ADD");
                json.put("chatId", chatId);
                String notification = oM.writeValueAsString(json);
                session.sendMessage(new TextMessage(notification));
            } catch (IOException e) {
                System.err.println("Error broadcasting new chat to <" + username + ">: " + e.getMessage());
            }
            System.out.println(" - CHAT SENT TO: " + recipient.getUsername());
        }
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(userSessions.keySet());
    }
}
