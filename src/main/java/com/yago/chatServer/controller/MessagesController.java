package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.*;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.repository.MessageRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.MessageService;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        if (chat instanceof GroupChat)
            webSocketHandler.broadcastMessageToChatGroup(WebSocketAction.MESSAGE_RECEIVED, message);
        else if (chat instanceof PrivateChat)
            webSocketHandler.broadcastMessageToPrivateChat(WebSocketAction.MESSAGE_RECEIVED, message);

        System.out.println("Mensaje enviado por " + user.getUsername() + ":\n - " + messageDTO.getMessageContent());

        return message;
    }

    @GetMapping("/history/{chatId}")
    public List<Message> getMessageHistory(@PathVariable Long chatId) {
        List<Message> msgs = messageRepository.findByChatId(chatId);
        for (Message msg : msgs) {
            if (msg.isDeleted()) msg.setMessageContent("Mensaje eliminado");
        }
        return msgs;
    }

    @GetMapping("/private/{userId}")
    public List<Message> getUserMessages(@PathVariable Long userId) {
        return messageRepository.findBySenderId(userId);
    }

    @PutMapping("/message/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        try {
            Message msg = messageRepository.findById(messageId).orElseThrow(() -> new EntityNotFoundException("Could not find message by id " + messageId));
            msg.setDeleted(true);
            messageRepository.save(msg);

            BaseChat chat = msg.getChat();

            if (chat instanceof GroupChat)
                webSocketHandler.broadcastMessageToChatGroup(WebSocketAction.MESSAGE_DELETED, msg);
            else if (chat instanceof PrivateChat)
                webSocketHandler.broadcastMessageToPrivateChat(WebSocketAction.MESSAGE_DELETED, msg);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Mensaje eliminado"));
        } catch (Exception e) {
            System.err.println("Error adding deleting message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }
}