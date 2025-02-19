package com.yago.ChatServer.controller;

import com.yago.ChatServer.dto.CreateChatDTO;
import com.yago.ChatServer.model.Chat;
import com.yago.ChatServer.repository.ChatRepository;
import com.yago.ChatServer.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatsController {
    private final ChatRepository chatRepository;
    private final ChatService chatService;

    @Autowired
    public ChatsController(ChatRepository chatRepository, ChatService chatService) {
        this.chatRepository = chatRepository;
        this.chatService = chatService;
    }

    @PostMapping("/create")
    public Chat addContact(@RequestBody CreateChatDTO createChatDTO) {
        return chatService.createChat(createChatDTO);
    }

    @GetMapping("/{userId}")
    public List<Chat> getUserChats(@PathVariable Long userId) {
        return chatRepository.findByParticipants_Id(userId);
    }
}
