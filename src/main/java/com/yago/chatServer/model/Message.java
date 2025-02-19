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
    private GroupChat groupChat;

    private String messageContent;

    private LocalDateTime timestamp;

    public Message() {
    }

    public Message(LocalDateTime timestamp, String messageContent, GroupChat groupChat, User sender) {
        this.timestamp = timestamp;
        this.messageContent = messageContent;
        this.groupChat = groupChat;
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

    public GroupChat getChat() {
        return groupChat;
    }

    public void setChat(GroupChat groupChat) {
        this.groupChat = groupChat;
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
                ", chat=" + groupChat +
                ", messageContent='" + messageContent + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
