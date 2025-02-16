package com.yago.ChatServer.controller;

import com.yago.ChatServer.ChatWebSocketHandler;
import com.yago.ChatServer.model.Mensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {
    private final List<Mensaje> mensajes = new ArrayList<>();

    private final ChatWebSocketHandler webSocketHandler;

    @Autowired
    public MensajeController(ChatWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/enviar")
    public Mensaje enviarMensaje(@RequestBody Mensaje mensaje) {
        mensaje.setId((long) (mensajes.size() + 1));
        mensaje.setTimestamp(LocalDateTime.now());
        mensajes.add(mensaje);
        System.out.println("Mensaje enviado por " + mensaje.getRemitente() + ": " + mensaje.getMensaje());

        //TODO: GESTIONAR LOS GRUPOS
        if (true) webSocketHandler.broadcastMessageToUser(mensaje.getMensaje(), mensaje.getDestinatario());
        else webSocketHandler.broadcastMessageToGroup(mensaje.getMensaje());

        return mensaje;
    }

    @GetMapping("/recibir/{destinatario}")
    public List<Mensaje> recibirMensajes(@PathVariable String destinatario) {
        List<Mensaje> mensajesDestinatario = new ArrayList<>();
        for (Mensaje mensaje : mensajes) {
            if (mensaje.getDestinatario().equals(destinatario)) {
                mensajesDestinatario.add(mensaje);
                System.out.println("Mensaje enviado a " + mensaje.getDestinatario() + ": " + mensaje.getMensaje());
            }
        }
        return mensajesDestinatario;
    }
}