package com.yago.ChatServer.controller;

import com.yago.ChatServer.websocket.ChatWebSocketHandler;
import com.yago.ChatServer.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {
    private final List<Message> mensajes = new ArrayList<>();

    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public MessagesController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody Message message) {
        message.setId((long) (mensajes.size() + 1));
        message.setTimestamp(LocalDateTime.now());
        mensajes.add(message);

        System.out.println("Mensaje enviado por " + message.getRemitente() + ": " + message.getMensaje());

        //TODO: GESTIONAR LOS GRUPOS, AÑADIR EN LA CLASE MESSAGE QUE EL DESTINATARIO SEA USUARIO O GRUPO (O QUIZAS OTRA CLASE?)
        if (true) webSocketHandler.broadcastMessageToPrivateChat(message.getMensaje(), message.getDestinatario());
        else webSocketHandler.broadcastMessageToChatGroup(message.getMensaje());

        return message;
    }

    @GetMapping("/recieve/{recipient}")
    public List<Message> recieveMessage(@PathVariable String recipient) {
        List<Message> mensajesDestinatario = new ArrayList<>();
        for (Message mensaje : mensajes) {
            if (mensaje.getDestinatario().equals(recipient)) {
                mensajesDestinatario.add(mensaje);
                System.out.println("Mensaje enviado a " + mensaje.getDestinatario() + ": " + mensaje.getMensaje());
            }
        }
        return mensajesDestinatario;
    }
}