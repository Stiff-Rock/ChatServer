package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.GroupChatDTO;
import com.yago.chatServer.dto.MessageDTO;
import com.yago.chatServer.model.GroupChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.model.WebSocketAction;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.repository.GroupChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.ChatService;
import com.yago.chatServer.websocket.AppWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de enpoints relacionados con los chats grupales
 */
@RestController
@RequestMapping("/api/groups")
public class GroupChatsController {
    @Autowired
    private AppWebSocketHandler webSocketHandler;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private BaseChatRepository baseChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessagesController messagesController;

    /**
     * Enpoint para crear un nuevo chat grupal
     *
     * @param gcd Solicitud de creación de grupo recibida
     * @return {@link GroupChat} si se ha creado o {@link ApiResponse} en caso de conflicto
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroupChat(@RequestBody GroupChatDTO gcd) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createGroupChat(gcd));
        } catch (Exception e) {
            System.err.println("Error creating group chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para la obtenición de la información de un {@link GroupChat} a partir de su id
     *
     * @param groupId Id del grupo del que se quiere obtener la información
     * @return objeto {@link GroupChat}
     */
    @GetMapping("/group/{groupId}")
    public GroupChat getGroupChat(@PathVariable Long groupId) {
        return groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Could not find GroupChat by Id " + groupId));
    }

    /**
     * Enpoint para añadir a un nuevo miembro al chat grupal
     *
     * @param groupId Id del grupo al que se va a añadir un nuevo miembro
     * @param userId  Id del usuario que se quiere añadir al grupo
     * @param userId  Id del usuario que se quiere añadir al grupo
     * @return {@link ApiResponse} con el resultado de la solicitud
     */
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
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

    /**
     * Enpoint para eliminar a un miembro de un chat grupal
     *
     * @param groupId Id del grupo del que se va a eliminar un miembro
     * @param userId  Id del usuario que se va a eliminar del grupo
     * @return {@link ApiResponse} con el resultado de la solicitud
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    @Transactional
    public ResponseEntity<ApiResponse> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
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
                baseChatRepository.deleteChatCascade(groupId);
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

    /**
     * Enpoint para dar privilegios de administrador a un miembro del grupo
     *
     * @param groupId Id del grupo
     * @param userId  Id del usuario al que se le van a dar los priviliegios
     * @return {@link ApiResponse} con el resultado de la solicitud
     */
    @PostMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<ApiResponse> addAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
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

    /**
     * Enpoint para revocar los privilegios de administrador a un miembro
     *
     * @param groupId Id del grupo
     * @param userId  Id del usuario al que se le van a revocar los privilegios
     * @return {@link ApiResponse} con el resultado de la solicituds
     */
    @DeleteMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<ApiResponse> removeAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
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

    /**
     * Endpoint para actualizar la foto del grupo
     *
     * @param groupId  Id del grupo
     * @param imageUrl Url de la imagen almacenada en la base de datos
     * @return {@link ApiResponse} con el resultado de la solicituds
     */
    @PostMapping("/{groupId}/photo")
    public ResponseEntity<ApiResponse> updateGroupChatPhoto(@PathVariable Long groupId, @RequestParam("imageUrl") String imageUrl) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Could not find group chat by id " + groupId));
            groupChat.setChatPhotoUrl(imageUrl);
            groupChatRepository.save(groupChat);

            return ResponseEntity.ok(new ApiResponse());
        } catch (EntityNotFoundException e) {
            System.err.println("Error updating group chat photo: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}