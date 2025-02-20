package com.yago.chatServer.controller;

import com.yago.chatServer.dto.PrivateChatDTO;
import com.yago.chatServer.model.BaseChat;
import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
public class PrivateChatsController {
    private final BaseChatRepository baseChatRepository;
    private final ChatService chatService;

    @Autowired
    public PrivateChatsController(BaseChatRepository baseChatRepository, ChatService chatService) {
        this.baseChatRepository = baseChatRepository;
        this.chatService = chatService;
    }

    //TODO: SI YA EXISTE DEVUELVE EL EXISTENTE
    @PostMapping("/private/create")
    public PrivateChat addContact(@RequestBody PrivateChatDTO privateChatDTO) {
        System.out.println("ADD CONTACT REQUEST: " + privateChatDTO);
        return chatService.createPrivateChat(privateChatDTO);
    }

    //TODO: QUIZAS SEPARAR POR BASECHAT, PRIVATECHAT Y GROUPCHAT
    @GetMapping("/chat/{chatId}")
    public BaseChat getChat(@PathVariable Long chatId) {
        return baseChatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with ID: " + chatId));
    }
}
