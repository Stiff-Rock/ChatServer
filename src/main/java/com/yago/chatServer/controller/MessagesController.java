package com.yago.chatServer.controller;

import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.Chat;
import com.yago.chatServer.model.Message;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.ChatRepository;
import com.yago.chatServer.repository.MessageRepository;
import com.yago.chatServer.repository.UserRepository;
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
    @Autowired
    private ChatWebSocketHandler webSocketHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/send")
    public Message sendMessage(@RequestBody MessageDTO messageDTO) {
        System.out.println("CREATING MESSAGE: " + messageDTO);

        Long chatId = messageDTO.getChatId();
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with ID: " + chatId));

        Long senderId = messageDTO.getSenderId();
        User user = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("User not found with ID: " + senderId));

        Message message = new Message(LocalDateTime.now(), messageDTO.getMessageContent(), chat, user);
        messageService.saveMessage(message);

        System.out.println("Mensaje enviado por " + user.getUsername() + ":\n - " + messageDTO.getMessageContent());

        //TODO: HANDLE WEBSOCKET DISCONNECTIONS
        if (chat.isGroupChat()) webSocketHandler.broadcastMessageToChatGroup(message);
        else webSocketHandler.broadcastMessageToPrivateChat(message);

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