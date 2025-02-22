package com.yago.chatServer.controller;

import com.yago.chatServer.dto.ApiResponse;
import com.yago.chatServer.dto.CredentialsDTO;
import com.yago.chatServer.model.BaseChat;
import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.BaseChatRepository;
import com.yago.chatServer.repository.UserRepository;
import com.yago.chatServer.service.UserService;
import com.yago.chatServer.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatWebSocketHandler webSocketHandler;

    @Autowired
    private BaseChatRepository baseChatRepository;

    /**
     * Endpoint para registrar un nuevo usuario con un username único.
     *
     * @param credentials Usuario que intenta registrarse.
     * @return Respuesta del servidor con el resultado dela acción.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody CredentialsDTO credentials) {
        System.out.println("Register attepmt with user <" + credentials.getUsername() + ">");
        String responseMessage = userService.registerUser(credentials);

        if (responseMessage.equals("Usuario registrado con éxito.")) {
            System.out.println("Register Successful");
            return ResponseEntity.status(201).body(new ApiResponse(responseMessage));
        } else {
            System.out.println("Register Failed");
            return ResponseEntity.status(409).body(new ApiResponse(responseMessage));
        }
    }

    /**
     * Endpoint para autenticar al usuario a través de sus credenciales.
     *
     * @param credentials Usuario que intenta inicar sesión.
     * @return Respuesta del servidor con el resultado dela acción.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody CredentialsDTO credentials) {
        System.out.println("LogIn attepmt with user <" + credentials.getUsername() + ">");
        User user = userService.loginUser(credentials);

        if (user != null) {
            System.out.println("LogIn Successful");
            return ResponseEntity.ok(user);
        } else {
            System.out.println("LogIn Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Inicio de sesión incorrecto"));
        }
    }

    /**
     * Endpoint para obtener la lista de usuarios online (conectados al WebSocket)
     *
     * @return Lista de {@link User}
     */
    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        return webSocketHandler.getOnlineUsers();
    }

    @GetMapping("/user/{userId}/chats")
    public List<BaseChat> getUserChats(@PathVariable Long userId) {
        //TODO: PUT THE NAME OF THE OPPOSIT USER IN THE ANME OF THE CHAT
        return baseChatRepository.findByParticipants_Id(userId);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(user);
    }
}
