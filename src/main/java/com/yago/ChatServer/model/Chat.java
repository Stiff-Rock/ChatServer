package com.yago.ChatServer.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long chatId;

    private String name;
    private boolean isGroupChat;

    @ManyToMany
    @JoinTable(name = "users_chats", joinColumns = @JoinColumn(name = "chatId"), inverseJoinColumns = @JoinColumn(name = "userId"))
    @JsonManagedReference
    private Set<User> participants;

    public Chat(String name, boolean isGroupChat, Set<User> participants) {
        this.name = name;
        this.isGroupChat = isGroupChat;
        this.participants = participants;
    }
}
