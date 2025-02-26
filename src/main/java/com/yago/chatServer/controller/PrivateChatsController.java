package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.PrivateChatDTO;
import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.service.ChatService;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
public class PrivateChatsController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private AppWebSocketHandler webSocketHandler;
    @Autowired
    private BaseChatRepository baseChatRepository;

    @PostMapping("/private/create")
    public PrivateChat addContact(@RequestBody PrivateChatDTO privateChatDTO) {
        return chatService.createPrivateChat(privateChatDTO);
    }

    @DeleteMapping("/private/delete/{chatId}")
    public ResponseEntity<?> deleteContact(@PathVariable Long chatId) {
        try {
            PrivateChat chat = (PrivateChat) baseChatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Could not find private chat by id " + chatId));
            baseChatRepository.deleteWithCascade(chat.getId());
            webSocketHandler.broadcastDeleteContact(chat, -1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Contacto eliminado"));
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }
}
