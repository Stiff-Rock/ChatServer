package com.yago.chatServer.service;

import com.yago.chatServer.dto.CredentialsDTO;
import com.yago.chatServer.model.User;
import com.yago.chatServer.model.UserPassword;
import com.yago.chatServer.repository.UserPasswordRepository;
import com.yago.chatServer.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPasswordService {
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;

    @Autowired
    public UserPasswordService(UserPasswordRepository userPasswordRepository, UserRepository userRepository) {
        this.userPasswordRepository = userPasswordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registra en la BBDD SQLite si no existe ya
     *
     * @param credentials Usuario que va a ser registrado
     * @return Mensaje del resultado de la operación
     */
    public String registerUser(CredentialsDTO credentials) {
        if (userRepository.existsByUsername(credentials.getUsername()))
            return "Error: El nombre de usuario ya está en uso.";

        String encryptedPassword = encryptPassword(credentials.getPassword());

        User user = new User(credentials.getUsername());
        UserPassword usrPwd = new UserPassword(user, encryptedPassword);

        userRepository.save(user);
        userPasswordRepository.save(usrPwd);

        return "Usuario registrado con éxito.";
    }

    /**
     * Autentica al usuario en la BBDD si las credenciales coniciden
     *
     * @param credentials Usuario que intenta iniciar sesión
     * @return Mensaje del resultado de la operación
     */
    public User loginUser(CredentialsDTO credentials) {
        User existingUser = userRepository.findByUsername(credentials.getUsername());
        UserPassword usrPwd = userPasswordRepository.findByUserUsername(existingUser.getUsername());

        if (!BCrypt.checkpw(credentials.getPassword(), usrPwd.getPassword())) return null;

        return existingUser;
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