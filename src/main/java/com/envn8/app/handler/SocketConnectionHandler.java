package com.envn8.app.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.envn8.app.models.CHAR;
import com.envn8.app.models.CharacterSequence;
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
    private Map<String, CharacterSequence> documentSequences = new HashMap<>();
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

    // Map<String, Object> charData = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        List<Map<String, Object>> charList = new ArrayList<>();
        // super.handleTextMessage(session, message);
        String documentId = getDocumentId(message);
        CharacterSequence sequences = documentSequences.computeIfAbsent(documentId, k -> new CharacterSequence());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageMap = mapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> operation = (Map<String, Object>) messageMap.get("operation");

        if (!roomSessions.containsKey(documentId)) {
            roomSessions.put(documentId, new ArrayList<>());
            System.out.println("*****Room Sessions: " );
        }

        roomSessions.get(documentId).add(session);
        if (operation != null) {
            String operationType = (String) operation.get("operationType");
            System.out.print("Ana geeet hena y3mna " + operationType + " docID" + documentId);
            if (operationType.equals("insertCharacter")) {

                Number indexNumber = (Number) operation.get("index");
                double index = indexNumber.doubleValue();
                String charValue = (String) operation.get("charValue");
                Object attributes = operation.get("attributes");
                String id = (String) operation.get("id");
                // Update the map
                CHAR insertedChar = sequences.insert(index, charValue, attributes, id);
                System.out
                        .print("Ana geeet Tany hena y3mna " + index + " " + charValue + " " + id + " " + insertedChar.getIndex());
                // Create a map to hold the updated character data
                Map<String, Object> updatedCharData = new HashMap<>();
                updatedCharData.put("index", insertedChar.getIndex());
                updatedCharData.put("charValue", insertedChar.getChar());
                updatedCharData.put("attributes", Collections.emptyMap());
                updatedCharData.put("id", insertedChar.getId());

                // Create a map to hold the updated sequence and the new character data
                Map<String, Object> responseData = new HashMap<>();

                charList.add(updatedCharData);

                // Use the list here
                responseData.put("sequence", charList);
                // responseData.put("sequence", sequences.getSequence());
                responseData.put("updatedChar", updatedCharData);

                // Convert the map to a JSON string
                String responseDataJson = new ObjectMapper().writeValueAsString(responseData);
                System.out.println("Response Data: " + responseDataJson);
                // Create a new TextMessage with the JSON string
                TextMessage updatedSequenceMessage = new TextMessage(responseDataJson);
                System.out.println("Updated Sequence Message: " + updatedSequenceMessage);
                // Send the message
                sendMessage(documentId, updatedSequenceMessage);
            }
        }
    }

    private void sendMessage(String roomId, TextMessage message) {
        if (roomSessions.containsKey(roomId)) {
            List<WebSocketSession> sessions = roomSessions.get(roomId);

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