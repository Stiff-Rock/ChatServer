package com.yago.chatServer.dto;

import com.yago.chatServer.model.Chat;
import com.yago.chatServer.model.User;
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