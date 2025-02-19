package com.yago.ChatServer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @ManyToMany(mappedBy = "participants")
    @JsonBackReference
    private Set<Chat> chats;

    public User(String username, Set<Chat> chats) {
        this.username = username;
        this.chats = chats;
    }
}
