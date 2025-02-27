package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.GroupChatDTO;
import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.model.WebSocketAction;
import com.yago.chatServer.repository.GroupChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.ChatService;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupChatsController {
    @Autowired
    private AppWebSocketHandler webSocketHandler;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessagesController messagesController;

    @PostMapping("/create")
    public ResponseEntity<?> createGroupChat(@RequestBody GroupChatDTO gcd) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createGroupChat(gcd));
        } catch (Exception e) {
            System.err.println("Error creating group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }

    @GetMapping("/group/{groupId}")
    public GroupChat getGroupChat(@PathVariable Long groupId) {
        return groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Could not find GroupChat by Id " + groupId));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("GroupChat not found with id: " + groupId));
            User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            if (!groupChat.getParticipants().contains(user)) {
                groupChat.getParticipants().add(user);
                user.getChats().add(groupChat);
            }

            groupChatRepository.save(groupChat);

            webSocketHandler.broadcastAddedToGroup(groupChat, user);

            webSocketHandler.broadCastGroupChatChange(groupChat, user, WebSocketAction.GROUP_CHAT_CHANGED);

            String msg = user.getUsername() + " se unió al grupo";
            User sys = userRepository.findByUsername("SYSTEM");
            MessageDTO messageDTO = new MessageDTO(sys.getId(), groupId, msg);
            messagesController.sendMessage(messageDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(msg));
        } catch (Exception e) {
            System.err.println("Error adding member to group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("GroupChat not found with id: " + groupId));
            User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            if (groupChat.getParticipants().remove(user)) {
                user.getChats().remove(groupChat);
            }

            groupChat.getAdmins().remove(user);

            String msg = user.getUsername() + " salió del grupo";
            if (groupChat.getParticipants().isEmpty()) {
                groupChatRepository.save(groupChat);
                groupChatRepository.delete(groupChat);
            } else {
                groupChatRepository.save(groupChat);
                User sys = userRepository.findByUsername("SYSTEM");
                MessageDTO messageDTO = new MessageDTO(sys.getId(), groupId, msg);
                messagesController.sendMessage(messageDTO);
                webSocketHandler.broadCastGroupChatChange(groupChat, user, WebSocketAction.GROUP_CHAT_DELETION);
            }
            webSocketHandler.broadcastRemovedFromGroup(groupChat, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(msg));
        } catch (Exception e) {
            System.err.println("Error removing member from group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<?> addAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("GroupChat not found with id: " + groupId));
            User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            groupChat.getAdmins().add(user);

            String msg = user.getUsername() + " ahora es administrador";
            User sys = userRepository.findByUsername("SYSTEM");
            MessageDTO messageDTO = new MessageDTO(sys.getId(), groupId, msg);
            messagesController.sendMessage(messageDTO);

            webSocketHandler.broadCastGroupChatChange(groupChat, user, WebSocketAction.GROUP_CHAT_CHANGED);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(msg));
        } catch (Exception e) {
            System.err.println("Error adding member to group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<?> removeAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("GroupChat not found with id: " + groupId));
            User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            boolean wasAdmin = groupChat.getAdmins().remove(user);
            if (!wasAdmin) throw new EntityNotFoundException("User was not admin");

            String msg = user.getUsername() + " ya no es administrador";
            User sys = userRepository.findByUsername("SYSTEM");
            MessageDTO messageDTO = new MessageDTO(sys.getId(), groupId, msg);
            messagesController.sendMessage(messageDTO);

            webSocketHandler.broadCastGroupChatChange(groupChat, user, WebSocketAction.GROUP_CHAT_CHANGED);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(msg));
        } catch (Exception e) {
            System.err.println("Error adding member to group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }
}