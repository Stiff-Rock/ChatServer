package com.yago.chatServer.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public AppWebSocketHandler appWebSocketHandler() {
        return new AppWebSocketHandler();
    }

    @Bean
    public GroupChatWebSocketHandler GroupChatWebSocketHandler() {
        return new GroupChatWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(appWebSocketHandler(), "/app")
                .setAllowedOrigins("*");

        registry.addHandler(GroupChatWebSocketHandler(), "/chat/group/{groupId}")
                .setAllowedOrigins("*");
    }
}
