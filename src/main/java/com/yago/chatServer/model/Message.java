package com.yago.chatServer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToMany
    @JoinTable(name = "message_recipients", joinColumns = @JoinColumn(name = "message_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> recipients = new HashSet<>();

    private String messageContent;

    private LocalDateTime timestamp;

    public Message(LocalDateTime timestamp, String messageContent, Set<User> recipients, Chat chat, User sender) {
        this.timestamp = timestamp;
        this.messageContent = messageContent;
        this.recipients = recipients;
        this.chat = chat;
        this.sender = sender;
    }
}
