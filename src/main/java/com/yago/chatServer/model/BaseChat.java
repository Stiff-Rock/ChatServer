package com.yago.chatServer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;

/**
 * Clase abstracta que representa un chat, tanto privado como grupal, y contiene atributos comunes
 * a ambos.
 * </p>
 * Atributos:
 * <p>
 * - {@link #id}: Id único del chat en la BBDD.
 * <p>
 * - {@link #participants}: Set de participantes de este chat.
 * <p>
 * - {@link #messages}: Lista de mensajes que han sido enviados en este chat.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "chats")

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = PrivateChat.class, name = "PrivateChat"), @JsonSubTypes.Type(value = GroupChat.class, name = "GroupChat")})
public abstract class BaseChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(name = "users_chats", joinColumns = @JoinColumn(name = "chatId"), inverseJoinColumns = @JoinColumn(name = "userId"))
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();

    public BaseChat() {
    }

    // Getters y setters comunes
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseChat chat = (BaseChat) o;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
