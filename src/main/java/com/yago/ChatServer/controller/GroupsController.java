package com.yago.ChatServer.controller;

import com.yago.ChatServer.ChatWebSocketHandler;
import com.yago.ChatServer.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {
    private final List<Group> groups = new ArrayList<>();

    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public GroupsController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody Group group) {
        groups.add(group);
        return ResponseEntity.status(201).body("Group created successfully.");
    }
}
