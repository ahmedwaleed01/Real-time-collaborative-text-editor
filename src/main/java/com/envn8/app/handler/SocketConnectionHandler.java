package com.envn8.app.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.envn8.app.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Service
public class SocketConnectionHandler extends TextWebSocketHandler implements ApplicationContextAware {
    private static final List<WebSocketSession> documentRooms = new ArrayList<WebSocketSession>();
    private Map<String, List<WebSocketSession>> roomSessions = new HashMap<>();
    private DocumentService documentService;
    private ApplicationContext applicationContext;
    public SocketConnectionHandler(DocumentService documentService) {
        this.documentService = documentService;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.documentService = applicationContext.getBean(DocumentService.class);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established");
        super.afterConnectionEstablished(session);
        documentRooms.add(session);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("afterConnectionClosed");

        super.afterConnectionClosed(session, status);
        if (!documentRooms.isEmpty() && documentRooms.contains(session)) {
            System.out.println("HENAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            documentRooms.remove(session);
            removeSessionFromRooms(session);
            System.out.println("   B3d    HENAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
    }

    private void removeSessionFromRooms(WebSocketSession session) {
        for (List<WebSocketSession> sessions : roomSessions.values()) {
            sessions.remove(session);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(message.getPayload(), Map.class);

        String documentId = (String) data.get("documentId");
        Map<String, Object> operation = (Map<String, Object>) data.get("operation");

        if (!roomSessions.containsKey(documentId)) {
            roomSessions.put(documentId, new ArrayList<>());
        }
        roomSessions.get(documentId).add(session);
        if (operation != null) {
            if (operation.get("type").equals("insertCharacter")) {
                System.out.println("AYwaaa hena ya negm");
                int position = (int) operation.get("position");
                System.out.println("AYwaaa hena ya negm2"+operation.get("character"));
                String characterObj = (String)operation.get("character");
    
                char character= characterObj.charAt(0);
                System.out.println("AYwaaa hena ya negm3");
                String  beforeId = (String) operation.get("beforeId");
        
                System.out.println("AYwaaa hena ya negm4");
                String  afterId = (String) operation.get("afterId");;
                // if(operation.get("afterId")!=null){
                //     afterId = (String) operation.get("afterId");
                // }
                // beforeId ="-1";
                // afterId="1";
                // Call the insertCharacter method in the DocumentService
                System.out.println("AYwaaa hena"+operation.get("type"));
                documentService.insertCharacter(documentId, position, beforeId, afterId, character);
            } else if (operation.get("type").equals("deleteCharacter")) {
                String characterId = (String) operation.get("characterId");
                // Call the deleteCharacter method in the DocumentService
                documentService.deleteCharacter(documentId, characterId);
            }
        } else {
            System.out.println("Operation is null");
        }
        // sendMessage(documentId, );
    }


    private void sendMessage(String roomId, TextMessage message) {
        if (roomSessions.containsKey(roomId)) {
            List<WebSocketSession> sessions = new ArrayList<>(roomSessions.get(roomId)); // added array here to avoid
                                                                                         // ConcurrentModificationException
            System.out.println(" Now Server sending response " + sessions);
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    } else {
                        roomSessions.remove(roomId, session);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getDocumentId(TextMessage message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(message.getPayload(), Map.class);
        System.out.println("Document ID: " + map.get("documentId"));
        return map.get("documentId");
    }

}