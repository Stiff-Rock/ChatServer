package com.yago.ChatServer.controller;

import com.yago.ChatServer.model.Message;
import com.yago.ChatServer.websocket.ChatWebSocketHandler;
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

        System.out.println("Mensaje enviado por " + message.getSender() + ": " + message.getMessageContent());

        boolean isChatGroup = message.getChat().isGroupChat();

        if (isChatGroup)            //TODO: ESTO ESTA FATAL
            webSocketHandler.broadcastMessageToPrivateChat(message.getMessageContent(), message.getRecipients().toArray()[0].toString());
        else webSocketHandler.broadcastMessageToChatGroup(message.getMessageContent());

        return message;
    }

    @GetMapping("/recieve/{recipient}")
    public List<Message> recieveMessage(@PathVariable String recipient) {
        List<Message> mensajesDestinatario = new ArrayList<>();
        for (Message message : mensajes) {
            //TODO: ESTO ESTA FATAL
            if (message.getRecipients().toArray()[0].toString().equals(recipient)) {
                mensajesDestinatario.add(message);
                //TODO: ESTO ESTA FATAL
                System.out.println("Mensaje enviado a " + message.getRecipients().toArray()[0].toString() + ": " + message.getMessageContent());
            }
        }
        return mensajesDestinatario;
    }
}