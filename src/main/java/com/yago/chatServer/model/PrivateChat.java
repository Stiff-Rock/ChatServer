package com.yago.chatServer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.*;

@Entity
@Table(name = "private_chats")
public class PrivateChat extends BaseChat {
    private String name;

    @Column(unique = true)
    private String uniqueHash;

    public PrivateChat() {
    }

    public PrivateChat(User user1, User user2) {
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        setParticipants(participants);
        generateUniqueHash();
        name = user1.getUsername() + "&" + user2.getUsername();
    }

    private void generateUniqueHash() {
        List<Long> ids = new ArrayList<>();
        for (User u : getParticipants()) {
            ids.add(u.getId());
        }
        Collections.sort(ids);
        uniqueHash = ids.size() >= 2 ? ids.get(0) + ":" + ids.get(1) : "";
    }

    // Getters y setters específicos de los chats privados
    public String getUniqueHash() {
        return uniqueHash;
    }

    public String getName() {
        return name;
    }

    //TODO: REVISAR DONE SE PUEDE SUAR EN EL SERVER
    // Metodo para obetener al usuario del set que no es el actual
    public User getContact(User currentUser) {
        User contact = null;
        for (User user : getParticipants()) {
            if (!user.equals(currentUser)) contact = user;
        }

        if (contact == null) {
            System.err.println("Error: could not find the other participant of private chat");
            return null;
        }

        return contact;
    }

    @Override
    public String toString() {
        return "PrivateChat{" + "chatId=" + getId() + ", name='" + name + '\'' + '}';
    }
}
