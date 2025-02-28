package com.yago.chatServer.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import java.util.Base64;
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

    @GetMapping("/photos/{solicitorId}")
    public ResponseEntity<String> getAllChatsPhotos(@PathVariable Long solicitorId, @RequestParam List<Long> chats) {
        try {
            User solicitor = userRepository.findById(solicitorId).orElseThrow(() -> new EntityNotFoundException("User not found: " + solicitorId));

            Map<Long, String> photoMap = new HashMap<>();
            for (Long id : chats) {
                BaseChat chat = baseChatRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Chat not found: " + id));

                byte[] photoBytes;
                if (chat instanceof PrivateChat privateChat) {
                    photoBytes = privateChat.getContact(solicitor).getProfilePicture();
                } else if (chat instanceof GroupChat groupChat) {
                    photoBytes = groupChat.getChatPhoto();
                } else {
                    continue;
                }

                if (photoBytes == null) {
                    photoBytes = new byte[0];
                }
                photoMap.put(chat.getId(), Base64.getEncoder().encodeToString(photoBytes));
            }

            ObjectMapper oM = new ObjectMapper();
            oM.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
            oM.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String json = oM.writeValueAsString(photoMap);

            return photoMap.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(json);
        } catch (Exception e) {
            System.err.println("Error returning photos json map: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
