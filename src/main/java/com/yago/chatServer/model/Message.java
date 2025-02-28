package com.yago.chatServer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "message", indexes = @Index(columnList = "chat_id"))
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonManagedReference
    private BaseChat chat;

    private String messageContent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageState messageState;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "message_read_by",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<User> readBy = new HashSet<>();

    private boolean deleted = false;

    public Message() {
    }

    public Message(LocalDateTime timestamp, String messageContent, BaseChat chat, User sender) {
        this.timestamp = timestamp;
        this.messageContent = messageContent;
        this.chat = chat;
        this.sender = sender;
        messageState = MessageState.SENT;
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

    public BaseChat getChat() {
        return chat;
    }

    public void setChat(BaseChat groupChat) {
        this.chat = groupChat;
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

    public MessageState getMessageState() {
        return messageState;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    public Set<User> getReadBy() {
        return readBy;
    }

    public void setReadBy(Set<User> readBy) {
        this.readBy = readBy;
    }

    public void markMsgReadByUser(User user) {
        readBy.add(user);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Message{" + "id=" + id + ", sender=" + sender + ", chat=" + chat + ", messageContent='" + messageContent + '\'' + ", timestamp=" + timestamp + ", messageState=" + messageState + ", deleted=" + deleted + '}';
    }
}
