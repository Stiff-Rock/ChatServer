package com.yago.chatServer.initializer;

import com.yago.chatServer.model.User;
import com.yago.chatServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Crea un usuario para el servidor para el envio de notificaciones
 */
@Component
public class DefaultUserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User sys = new User("SYSTEM");
            userRepository.save(sys);
        }
    }
}
