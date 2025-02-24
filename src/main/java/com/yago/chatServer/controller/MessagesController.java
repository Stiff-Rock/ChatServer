package com.yago.chatServer.controller;

import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.*;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.repository.MessageRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.MessageService;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {
    @Autowired
    private AppWebSocketHandler webSocketHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BaseChatRepository baseChatRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/send")
    public Message sendMessage(@RequestBody MessageDTO messageDTO) {
        Long chatId = messageDTO.getChatId();
        BaseChat chat = baseChatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Chat not found with ID: " + chatId));

        Long senderId = messageDTO.getSenderId();
        User user = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + senderId));

        Message message = new Message(LocalDateTime.now(), messageDTO.getMessageContent(), chat, user);
        messageService.saveMessage(message);

        //TODO: HANDLE WEBSOCKET DISCONNECTIONS
        if (chat instanceof GroupChat) webSocketHandler.broadcastMessageToChatGroup(message);
        else if (chat instanceof PrivateChat) webSocketHandler.broadcastMessageToPrivateChat(message);

        System.out.println("Mensaje enviado por " + user.getUsername() + ":\n - " + messageDTO.getMessageContent());

        return message;
    }

    @GetMapping("/history/{chatId}")
    public List<Message> getMessageHistory(@PathVariable Long chatId) {
        return messageRepository.findByChatId(chatId);
    }

    @GetMapping("/private/{userId}")
    public List<Message> getUserMessages(@PathVariable Long userId) {
        return messageRepository.findBySenderId(userId);
    }
}