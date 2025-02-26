package com.yago.chatServer.model;

import jakarta.persistence.*;
//TODO: CASCADE DELETIONS
@Entity
public class UserPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String password;

    public UserPassword() {
    }

    public UserPassword(User user, String password) {
        this.user = user;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserPassword{" +
                "id=" + id +
                ", user=" + user +
                ", password='" + password + '\'' +
                '}';
    }
}
