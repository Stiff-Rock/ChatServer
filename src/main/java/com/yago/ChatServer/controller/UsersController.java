package com.yago.ChatServer.controller;

import com.yago.ChatServer.ChatWebSocketHandler;
import com.yago.ChatServer.model.ApiResponse;
import com.yago.ChatServer.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private Map<String, User> users = new HashMap<>();

    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public UsersController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody User user) {
        if (user.getPwd().isBlank())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse("Error: Contraseña vacía."));

        hashPassword(user);

        User existingUser = users.get(user.getUsername());

        if (existingUser != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse("Error: El nombre de usuario ya está en uso."));


        users.put(user.getUsername(), user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Usuario registrado con éxito."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> logInUser(@RequestBody User user) {
        if (user.getPwd().isBlank())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse("Error: Contraseña vacía."));

        System.out.println(user.getUsername() + " attempting login...");

        hashPassword(user);

        User existingUser = users.get(user.getUsername());

        if (existingUser != null && existingUser.getPwd().equals(user.getPwd()))
            return ResponseEntity.ok(new ApiResponse("Inicio de sesión exitoso"));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Error: Credenciales incorrectas"));
    }

    //TODO: INVESTIGAR MANERA DE TENER LA LISTA GLOBAL DE USUARIOS MODO SPRINGBOOT

    /**
     * Endpoint para obtener la lista de usuarios online (conectados al WebSocket)
     *
     * @return Lista de {@link User}
     */
    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        for (String username : webSocketHandler.getOnlineUsers()) {
            User user = users.get(username);
            if (user != null) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    /**
     * Hashea la contraseña introducida por el usuario por seguridad
     *
     * @param user Usuario cuya contraseña va a ser hasheada
     */
    private void hashPassword(User user) {
        String salt = BCrypt.gensalt(12);
        String hashedPwd = BCrypt.hashpw(user.getPwd(), salt);
        user.setPwd(hashedPwd);
    }
}
