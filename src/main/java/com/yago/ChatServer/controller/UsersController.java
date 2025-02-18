package com.yago.ChatServer.controller;

import com.yago.ChatServer.dto.UserDTO;
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
    private final UserService userService;
    private final UserRepository userRepository;
    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public UsersController(UserService userService, UserRepository userRepository, ChatWebSocketHandler webSocketHandler) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Endpoint para registrar un nuevo usuario con un username único.
     *
     * @param user Usuario que intenta registrarse.
     * @return Respuesta del servidor con el resultado dela acción.
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
     * Endpoint para autenticar al usuario a través de sus credenciales.
     *
     * @param user Usuario que intenta inicar sesión.
     * @return Respuesta del servidor con el resultado dela acción.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        System.out.println("LogIn attepmt with user <" + user.getUsername() + ">");
        UserDTO userDto = userService.loginUser(user);

        if (userDto != null) {
            System.out.println("LogIn Successful");
            return ResponseEntity.ok(userDto);
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
    public List<UserDTO> getOnlineUsers() {
        List<UserDTO> onlineUsers = new ArrayList<>();
        List<String> onlineUsernames = webSocketHandler.getOnlineUsers();

        for (String username : onlineUsernames) {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                onlineUsers.add(new UserDTO(user.getId(), username));
            }
        }

        return onlineUsers;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) return ResponseEntity.notFound().build();

        UserDTO userDto = new UserDTO(user.getId(), user.getUsername());
        return ResponseEntity.ok(userDto);
    }
}
