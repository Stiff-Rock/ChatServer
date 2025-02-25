package com.yago.chatServer.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yago.chatServer.model.*;
import com.yago.chatServer.repository.PrivateChatRepository;
import com.yago.chatServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Set;

import static com.yago.chatServer.model.WebSocketAction.*;
import static com.yago.chatServer.websocket.WebSocketMsgManager.msgToJson;
import static com.yago.chatServer.websocket.WebSocketMsgManager.oM;

public class AppWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<User, WebSocketSession> userSessions = new HashMap<>();
    private final HashMap<WebSocketSession, User> sessionUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrivateChatRepository privateChatRepository;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        if (session.getUri() == null) {
            System.err.println("Error obtaining session URI");
            return;
        }

        String query = session.getUri().getQuery();

        if (query == null) {
            System.err.println("Error: query is null");
            return;
        }

        if (!query.startsWith("username=")) {
            System.err.println("Error: query does not contain username information");
            return;
        }

        String username = URLDecoder.decode(query.substring("username=".length()), StandardCharsets.UTF_8);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            System.err.println("Error: could not find user with username: " + username);
            return;
        }

        if (!userSessions.containsKey(user) && !sessionUsers.containsValue(user)) {
            userSessions.put(user, session);
            sessionUsers.put(session, user);
            System.out.println("WebSocket connection established with user <" + username + ">");
            broadcastUserStatusChange(user, WebSocketAction.USER_CONNECTED);
        } else {
            System.err.println("Error: User with username <" + username + "> already connected to the WebSocket");
            WebSocketSession prevSession = userSessions.get(user);
            sessionUsers.remove(prevSession);
            userSessions.put(user, session);
            sessionUsers.put(session, user);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        User user = sessionUsers.get(session);
        if (user != null) {
            userSessions.remove(user);
            sessionUsers.remove(session);
            System.out.println("Connection closed for user <" + user.getUsername() + ">");
            broadcastUserStatusChange(user, USER_DISCONNECTED);
        } else {
            StringBuilder err = new StringBuilder("Error: Could not find user for the disconnected session\n");
            err.append("Currently connected sessions:\n");
            for (User u : sessionUsers.values()) {
                err.append(" - ").append(u.getUsername());
            }
            System.err.println(err);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode rootNode = oM.readTree(payload);
        WebSocketAction action = WebSocketAction.valueOf(rootNode.path("action").asText());
        JsonNode contentNode = rootNode.path("content");
        if (action.equals(GET_CONTACTS_ONLINE_STATUS)) {
            User user = oM.treeToValue(contentNode, User.class);
            getUserContactsStatus(user, session);
        } else if (action.equals(GET_USER_ONLINE_STATUS)) {
            User user = oM.treeToValue(contentNode, User.class);
            getUserOnlineStatus(user, session);
        }
    }

    private void getUserContactsStatus(User user, WebSocketSession session) {
        List<User> contacts = privateChatRepository.findUserContacts(user);
        for (User contact : contacts) {
            WebSocketAction status = userSessions.containsKey(contact) ? USER_CONNECTED : USER_DISCONNECTED;
            ObjectNode jsonMessageNode = msgToJson(status, contact);
            if (jsonMessageNode == null) return;
            try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting user status to user <" + user.getUsername() + ">: " + e.getMessage());
            }
        }
    }

    private void getUserOnlineStatus(User user, WebSocketSession session) {
        WebSocketAction status = userSessions.containsKey(user) ? USER_CONNECTED : USER_DISCONNECTED;
        ObjectNode jsonMessageNode = msgToJson(status, user);
        if (jsonMessageNode == null) return;
        try {
            session.sendMessage(new TextMessage(jsonMessageNode.toString()));
        } catch (IOException e) {
            String username = sessionUsers.get(session).getUsername();
            System.err.println("Error broadcasting user status to user <" + username + ">: " + e.getMessage());
        }
    }

    public void broadcastMessageToChatGroup(WebSocketAction action, Message message) {
        System.out.println("BROADCASTING MESSAGE TO GROUP");

        if (message.isDeleted()) message.setMessageContent("Mensaje eliminado");

        ObjectNode jsonMessageNode = msgToJson(action, message);
        if (jsonMessageNode == null) return;

        GroupChat groupChat = (GroupChat) message.getChat();
        for (User user : groupChat.getParticipants()) {
            if (user.equals(message.getSender())) continue;
            WebSocketSession session = userSessions.get(user);
            if (session != null) try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting message \"" + message + "\": " + e.getMessage());
            }
        }
    }

    public void broadcastMessageToPrivateChat(WebSocketAction action, Message msg) {
        System.out.println("BROADCASTING MESSAGE TO PRIVATE CHAT");

        PrivateChat chat = (PrivateChat) msg.getChat();
        Set<User> users = chat.getParticipants();
        if (msg.isDeleted()) {
            msg.setMessageContent("Mensaje eliminado");
        } else {
            users.remove(msg.getSender());
        }

        if (users.isEmpty()) {
            System.err.println("COULD NOT FIND USERS OF PRIVATE CHAT");
            return;
        }

        for (User user : users) {
            WebSocketSession session = userSessions.get(user);
            if (session != null && session.isOpen()) {
                ObjectNode jsonMessageNode = msgToJson(action, msg);
                if (jsonMessageNode == null) return;

                try {
                    session.sendMessage(new TextMessage(jsonMessageNode.toString()));
                } catch (IOException e) {
                    System.err.println("Error broadcasting message \"" + msg + "\" to user <" + user.getUsername() + ">: " + e.getMessage());
                }
            }
        }
    }

    public void broadcastNewChat(BaseChat chat, Long creatorId) {
        ObjectNode jsonMessageNode = msgToJson(ADD_CHAT, chat);
        if (jsonMessageNode == null) return;

        for (User recipient : chat.getParticipants()) {
            if (recipient.getId().equals(creatorId)) continue;
            if (!userSessions.containsKey(recipient)) continue;

            WebSocketSession session = userSessions.get(recipient);
            if (session == null) {
                System.err.println("Could not broadcast to session of user: " + recipient.getUsername() + ": Session is null");
                return;
            }

            try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting new chat to <" + recipient.getUsername() + ">: " + e.getMessage());
            }
        }
    }

    //TODO: DELETE CONTACT
    public void broadcastDeleteContact(BaseChat chat, Long creatorId) {
        ObjectNode jsonMessageNode = msgToJson(DELETE_CONTACT, chat);
        if (jsonMessageNode == null) return;

        for (User recipient : chat.getParticipants()) {
            if (recipient.getId().equals(creatorId)) continue;
            if (!userSessions.containsKey(recipient)) continue;

            WebSocketSession session = userSessions.get(recipient);
            if (session == null) {
                System.err.println("Could not broadcast to session of user: " + recipient.getUsername() + ": Session is null");
                return;
            }

            try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting new chat to <" + recipient.getUsername() + ">: " + e.getMessage());
            }
        }
    }

    public void broadcastRemovedFromGroup(GroupChat chat, User removedUser) {
        ObjectNode jsonMessageNodeRemovedUser = msgToJson(DELETE_CONTACT, chat);
        if (jsonMessageNodeRemovedUser == null) return;

        WebSocketSession removedUsersession = userSessions.get(removedUser);
        if (removedUsersession != null) try {
            removedUsersession.sendMessage(new TextMessage(jsonMessageNodeRemovedUser.toString()));
        } catch (IOException e) {
            System.err.println("Error broadcasting new chat to <" + removedUser.getUsername() + ">: " + e.getMessage());
        }
    }

    public void broadcastAddedToGroup(GroupChat chat, User addedUser) {
        ObjectNode jsonMessageNode = msgToJson(ADD_CHAT, chat);
        if (jsonMessageNode == null) return;

        WebSocketSession session = userSessions.get(addedUser);
        if (session == null) {
            System.err.println("Could not broadcast to session of user: " + addedUser.getUsername() + ": Session is null");
            return;
        }

        try {
            session.sendMessage(new TextMessage(jsonMessageNode.toString()));
        } catch (IOException e) {
            System.err.println("Error broadcasting new chat to <" + addedUser.getUsername() + ">: " + e.getMessage());
        }
    }

    private void broadcastUserStatusChange(User user, WebSocketAction action) {
        ObjectNode jsonMessageNode = msgToJson(action, user);
        if (jsonMessageNode == null) return;
        System.out.println("BROADCASTING USER STATUS CHANGE: " + user.getUsername());
        for (BaseChat baseChat : user.getChats()) {
            WebSocketSession session;
            User recipient;
            if (baseChat instanceof PrivateChat) {
                recipient = ((PrivateChat) baseChat).getContact(user);
                session = userSessions.get(recipient);
                if (session == null) continue;

            } else continue;
            try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting user status to user <" + recipient.getUsername() + ">: " + e.getMessage());
            }
        }
    }

    public void broadCastGroupChatChange(GroupChat groupChat, User user, WebSocketAction action) {
        ObjectNode jsonMessageNode = oM.createObjectNode();
        jsonMessageNode.put("action", action.name());

        ObjectNode combinedData = oM.createObjectNode();
        JsonNode groupChatJson = WebSocketMsgManager.oM.valueToTree(groupChat);
        JsonNode userJson = WebSocketMsgManager.oM.valueToTree(user);
        combinedData.set("groupChat", groupChatJson);
        combinedData.set("user", userJson);
        jsonMessageNode.set("content", combinedData);

        for (User recipient : groupChat.getParticipants()) {
            WebSocketSession session = userSessions.get(recipient);
            if (session != null) try {
                session.sendMessage(new TextMessage(jsonMessageNode.toString()));
            } catch (IOException e) {
                System.err.println("Error broadcasting GroupChat change \"" + action.name() + "\": " + e.getMessage());
            }
        }
    }

    public List<User> getOnlineUsers() {
        return new ArrayList<>(userSessions.keySet());
    }
}
