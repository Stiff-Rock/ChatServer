package com.yago.ChatServer.dto;

import com.yago.ChatServer.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatDTO {
    private String chatName;
    private boolean isGroupChat;
    private Set<User> participants;
}
