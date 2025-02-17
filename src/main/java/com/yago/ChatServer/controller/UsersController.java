package com.yago.ChatServer.controller;

import com.yago.ChatServer.model.ApiResponse;
import com.yago.ChatServer.model.User;
import com.yago.ChatServer.repository.UserRepository;
import com.yago.ChatServer.service.UserService;
import com.yago.ChatServer.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public UsersController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * @param user
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody User user) {
        System.out.println("Register attepmt with user <" + user.getUsername() + ">");
        String responseMessage = userService.registerUser(user);

        if (responseMessage.equals("Usuario registrado con éxito.")) {
            System.out.println("Register Successful");
            return ResponseEntity.status(201).body(new ApiResponse(responseMessage));
        } else {
            System.out.println("Register Failed");
            return ResponseEntity.status(409).body(new ApiResponse(responseMessage));
        }
    }

    /**
     * @param user
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody User user) {
        System.out.println("LogIn attepmt with user <" + user.getUsername() + ">");
        String responseMessage = userService.loginUser(user);

        if (responseMessage.equals("Inicio de sesión exitoso.")) {
            System.out.println("LogIn Successful");
            return ResponseEntity.ok(new ApiResponse(responseMessage));
        } else {
            System.out.println("LogIn Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(responseMessage));
        }
    }

    /**
     * Endpoint para obtener la lista de usuarios online (conectados al WebSocket)
     *
     * @return Lista de {@link User}
     */
    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        List<String> onlineUsernames = webSocketHandler.getOnlineUsers();

        for (String username : onlineUsernames) {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }
}
