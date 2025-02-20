package com.yago.chatServer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Set;
//TODO: CASCADE DELETIONS
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @ManyToMany(mappedBy = "participants")
    @JsonBackReference
    private Set<BaseChat> chats;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<BaseChat> getChats() {
        return chats;
    }

    public void setChats(Set<BaseChat> groupChats) {
        this.chats = groupChats;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + '}';
    }
}
