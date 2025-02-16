package com.yago.ChatServer.controller;

import com.yago.ChatServer.ChatWebSocketHandler;
import com.yago.ChatServer.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final List<User> users = new ArrayList<>();

    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public UsersController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        for (User usr : users) {
            if (usr.getUsername().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: El nombre de usuario ya está en uso.");
            }
        }
        users.add(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> logInUser(@RequestBody User user) {
        for (User usr : users) {
            if (usr.getUsername().equals(user.getUsername()) && usr.getPwd().equals(user.getPwd())) {
                return ResponseEntity.ok("Inicio de sesión exitoso");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Credenciales incorrectas");
    }

    //TODO: INNVESTIGAR AMNERA DE TEENER LA LSITA GLOBAL DE USUARIOS A MODO SPRINBOOT
    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        Set<String> userNamesSet = new HashSet<>();
        for (User usr : users) {
            userNamesSet.add(usr.getUsername());
        }

        List<User> onlineUsers = new ArrayList<>();
        for (String username : webSocketHandler.getOnlineUsers()) {
            if (userNamesSet.contains(username)) {
                for (User usr : users) {
                    if (usr.getUsername().equals(username)) {
                        onlineUsers.add(usr);
                        break;
                    }
                }
            }
        }
        return onlineUsers;
    }
}
