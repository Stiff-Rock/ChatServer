package com.yago.chatServer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yago.chatServer.model.WebSocketAction;

import java.io.IOException;

public class WebSocketMsgManager {
    public static final ObjectMapper oM = new ObjectMapper().registerModule(new JavaTimeModule());

    public static ObjectNode msgToJson(WebSocketAction action, Object content) {
        ObjectNode jsonMessageNode = null;
        try {
            jsonMessageNode = oM.createObjectNode();
            jsonMessageNode.put("action", action.name());
            jsonMessageNode.put("content", oM.writeValueAsString(content));
        } catch (IOException e) {
            System.err.println("Error creating notification JSON: " + e.getMessage());
        }
        return jsonMessageNode;
    }
}
