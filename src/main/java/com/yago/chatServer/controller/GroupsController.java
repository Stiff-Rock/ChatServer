package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.CreateChatDTO;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.ChatService;
import com.yago.chatServer.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {
    @Autowired
    private ChatWebSocketHandler webSocketHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createGroupChat(@RequestBody CreateChatDTO ccd) {
        try {
            System.out.println("CREATE CHAT REQUEST: " + ccd);
            chatService.createChat(ccd);
        } catch (Exception e) {
            System.err.println("Error creating group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Group created successfully."));
    }
}
