package com.yago.chatServer.controller;

import com.yago.chatServer.dto.CreateChatDTO;
import com.yago.chatServer.model.Chat;
import com.yago.chatServer.repository.ChatRepository;
import com.yago.chatServer.service.ChatService;
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
        System.out.println("CREATING CHAT: " + createChatDTO.toString());
        return chatService.createChat(createChatDTO);
    }

    @GetMapping("/{userId}")
    public List<Chat> getUserChats(@PathVariable Long userId) {
        return chatRepository.findByParticipants_Id(userId);
    }
}
