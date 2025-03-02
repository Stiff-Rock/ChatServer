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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Controlado de los endpoints relacionados con los chats privados
 */
@RestController
@RequestMapping("/api/chats")
public class PrivateChatsController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private AppWebSocketHandler webSocketHandler;
    @Autowired
    private BaseChatRepository baseChatRepository;

    /**
     * Endpoint para crear un nuevo chat privado entre dos usuarios
     *
     * @param privateChatDTO Solicitud de creación del chat
     * @return Objeto {@link PrivateChat} resultante
     */
    @PostMapping("/private/create")
    public PrivateChat addContact(@RequestBody PrivateChatDTO privateChatDTO) {
        return chatService.createPrivateChat(privateChatDTO);
    }

    /**
     * Endpoint para eliminar un chat privado
     *
     * @param chatId Id del chat a borrar
     * @return {@link ApiResponse} con el resultado de la solicitud
     */
    @DeleteMapping("/private/delete/{chatId}")
    @Transactional
    public ResponseEntity<ApiResponse> deleteContact(@PathVariable Long chatId) {
        try {
            PrivateChat chat = (PrivateChat) baseChatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Could not find private chat by id " + chatId));
            baseChatRepository.deleteChatCascade(chatId);
            webSocketHandler.broadcastDeleteContact(chat, -1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Contacto eliminado"));
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }
}
