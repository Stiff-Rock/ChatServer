package com.yago.chatServer.controller;

import com.yago.chatServer.dto.CreateChatDTO;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.repository.ChatRepository;
import com.yago.chatServer.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public GroupChat addContact(@RequestBody CreateChatDTO createChatDTO) {
        return chatService.createChat(createChatDTO);
    }

    @GetMapping("/chat/{chatId}")
    public GroupChat getChat(@PathVariable Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with ID: " + chatId));
    }
}
