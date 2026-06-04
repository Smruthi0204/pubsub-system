package com.PubSub.System.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String path = session.getUri().getPath(); 
        String subscriberName = path.substring(path.lastIndexOf('/') + 1);

        sessions.put(subscriberName, session);
    }

    public void sendMessage(String subscriberName, String message) throws Exception {
        WebSocketSession session = sessions.get(subscriberName);

        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}