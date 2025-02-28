package com.yago.chatServer.controller;

import com.yago.chatServer.model.BaseChat;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
public class ChatsController {
    @Autowired
    private BaseChatRepository baseChatRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/photos/{solicitorId}")
    public ResponseEntity<Map<BaseChat, byte[]>> getAllChatsPhotos(@RequestBody List<BaseChat> chats, @PathVariable Long solicitorId) {
        try {
            User solicitor = userRepository.findById(solicitorId).orElseThrow(() -> new EntityNotFoundException("Could not find user with id " + solicitorId));

            Map<BaseChat, byte[]> map = new HashMap<>();
            for (BaseChat userChat : chats) {
                BaseChat chat = baseChatRepository.findById(userChat.getId()).orElseThrow(() -> new EntityNotFoundException("Could not find chat with id " + userChat.getId()));
                byte[] photoBytes;
                if (chat instanceof PrivateChat) {
                    photoBytes = ((PrivateChat) chat).getContact(solicitor).getProfilePicture();
                } else if (chat instanceof GroupChat) {
                    photoBytes = ((GroupChat) chat).getChatPhoto();
                } else continue;

                map.put(userChat, photoBytes);
            }

            if (map.isEmpty()) return ResponseEntity.notFound().build();

            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
