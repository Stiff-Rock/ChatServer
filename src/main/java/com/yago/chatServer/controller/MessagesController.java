package com.yago.chatServer.controller;

import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.Message;
import com.yago.chatServer.repository.MessageRepository;
import com.yago.chatServer.service.MessageService;
import com.yago.chatServer.websocket.ChatWebSocketHandler;
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


    @Autowired
    public MessagesController(ChatWebSocketHandler webSocketHandler, MessageService messageService, MessageRepository messageRepository) {
        this.webSocketHandler = webSocketHandler;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody MessageDTO messageDTO) {
        Message message = new Message(LocalDateTime.now(), messageDTO.getMessageContent(), messageDTO.getChat().getParticipants(), messageDTO.getChat(), messageDTO.getSender());
        messageService.saveMessage(message);

        System.out.println("Mensaje enviado por " + messageDTO.getSender() + ": " + messageDTO.getMessageContent());

        boolean isChatGroup = messageDTO.getChat().isGroupChat();

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
}