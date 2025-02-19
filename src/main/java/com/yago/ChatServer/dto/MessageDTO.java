package com.yago.ChatServer.dto;

import com.yago.ChatServer.model.Chat;
import com.yago.ChatServer.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private User sender;
    private Chat chat;
    private String messageContent;
}