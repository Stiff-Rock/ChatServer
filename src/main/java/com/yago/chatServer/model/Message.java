package com.yago.chatServer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
//TODO: CASCADE DELETIONS
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

    private String messageContent;

    private LocalDateTime timestamp;

    public Message() {
    }

    public Message(LocalDateTime timestamp, String messageContent, Chat chat, User sender) {
        this.timestamp = timestamp;
        this.messageContent = messageContent;
        this.chat = chat;
        this.sender = sender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sender=" + sender +
                ", chat=" + chat +
                ", messageContent='" + messageContent + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
