package com.yago.ChatServer.service;

import com.yago.ChatServer.model.User;
import com.yago.ChatServer.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Registra en la BBDD SQLite si no existe ya
     *
     * @param user Usuario que va a ser registrado
     * @return Mensaje del resultado de la operación
     */
    public String registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Error: El nombre de usuario ya está en uso.";
        }

        String encryptedPassword = encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);

        userRepository.save(user);
        return "Usuario registrado con éxito.";
    }

    /**
     * Autentica al usuario en la BBDD si las credenciales coniciden
     *
     * @param user Usuario que intenta iniciar sesión
     * @return Mensaje del resultado de la operación
     */
    public String loginUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) return "Error: Usuario no encontrado.";
        
        if (!BCrypt.checkpw(user.getPassword(), existingUser.getPassword())) return "Error: Credenciales incorrectas.";

        return "Inicio de sesión exitoso.";
    }

    /**
     * Encripta la contraseña dada y la deuelve en Hash
     *
     * @param pwd Contraseña a encriptar
     * @return Contraseña en Hash
     */
    private String encryptPassword(String pwd) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(pwd, salt);
    }
}