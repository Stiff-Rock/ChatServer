package com.yago.ChatServer.controller;

import com.yago.ChatServer.model.Chat;
import com.yago.ChatServer.model.Message;
import com.yago.ChatServer.repository.ChatRepository;
import com.yago.ChatServer.repository.MessageRepository;
import com.yago.ChatServer.service.MessageService;
import com.yago.ChatServer.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {
    private final ChatWebSocketHandler webSocketHandler;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    @Autowired
    public MessagesController(ChatWebSocketHandler webSocketHandler, MessageService messageService, MessageRepository messageRepository, ChatRepository chatRepository) {
        this.webSocketHandler = webSocketHandler;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody Message message) {
        message.setTimestamp(LocalDateTime.now());
        messageService.saveMessage(message);

        System.out.println("Mensaje enviado por " + message.getSender() + ": " + message.getMessageContent());

        boolean isChatGroup = message.getChat().isGroupChat();

        if (isChatGroup) webSocketHandler.broadcastMessageToPrivateChat(message);
        else webSocketHandler.broadcastMessageToChatGroup(message);

        return message;
    }

    @GetMapping("/recieve/{messageId}")
    public Message recieveMessage(@PathVariable Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));
    }

    @GetMapping("/history/{chatId}")
    public List<Message> getMessageHistory(@PathVariable Long chatId) {
        List<Message> messageHistory = new ArrayList<>();

        return messageHistory;
    }

    @GetMapping("/private/{userId}")
    public List<Message> getUserMessages(@PathVariable Long userId) {
        List<Message> userMessages = new ArrayList<>();

        return userMessages;
    }

    @GetMapping("/chats/{userId}")
    public List<Chat> getUserChats(@PathVariable Long userId) {
        List<Chat> userChats = chatRepository.findByParticipants_Id(userId);

        return userChats;
    }
}