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
import java.util.Comparator;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private DocumentService documentService;
    private ApplicationContext applicationContext;
    private final Object lock = new Object();

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

    @SuppressWarnings("unchecked")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("afterConnectionClosed");

        super.afterConnectionClosed(session, status);
        if (!documentRooms.isEmpty() && documentRooms.contains(session)) {
            System.out.println("HENAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            List<CHAR> contentList = (List<CHAR>) session.getAttributes().get("sequence");
            if (contentList != null) {
                List<CHAR> newList = new ArrayList<>();
                if (contentList.size() > 2) {
                    newList = new ArrayList<>(contentList.subList(1, contentList.size() - 1)); // first t3deel
                }
                saveDocument((String) session.getAttributes().get("documentId"), newList);
            }
            documentRooms.remove(session);
            removeSessionFromRooms(session);
            System.out.println("   B3d  HENAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ");
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
        // super.handleTextMessage(session, message);
        String documentId = getDocumentId(message);
        CharacterSequence sequences;
        if (documentSequences.get(documentId) == null) {
            List<CHAR> Oldcontent = documentService.getContent(documentId);
            sequences = documentSequences.computeIfAbsent(documentId, k -> new CharacterSequence(Oldcontent));

        }
        System.out.println("Service ll" + documentSequences.get(documentId));
        sequences = documentSequences.get(documentId);

        // CharacterSequence sequences = new CharacterSequence(content);

        documentSequences.put(documentId, sequences);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageMap = mapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> operation = (Map<String, Object>) messageMap.get("operation");

        if (!roomSessions.containsKey(documentId)) {
            roomSessions.put(documentId, new ArrayList<>());
        }

        roomSessions.get(documentId).add(session);
        if (operation != null) {
            String operationType = (String) operation.get("operationType");
            System.out.print("Ana geeet hena y3mna " + operationType + " docID" + documentId);
            if (operationType.equals("insertCharacter")) {

                String id = (String) operation.get("id");
                int index = (int) operation.get("indexStart");
                String charValue = (String) operation.get("charValue");
                Object attributes = operation.get("attributes");

                List<CHAR> relatives = sequences.getRelativeIndex(index);
                double startIndex = relatives.get(0).getIndex();
                double endIndex = relatives.get(1).getIndex();
                System.out.println("END ELEMENT" + relatives.get(1).getChar());
                if (startIndex == -1 && endIndex == -1) {
                    startIndex = 0;
                    endIndex = 0;
                }
                CHAR insertedChar = sequences.insert(startIndex, endIndex, charValue, attributes, id);
                Map<String, Object> diff = new HashMap<>();
                // diff.put("operationType", "insertCharacter");
                diff.put("id", insertedChar.getId());
                diff.put("index", insertedChar.getIndex());
                diff.put("charValue", insertedChar.getChar());
                diff.put("bold", insertedChar.getBold());
                diff.put("italic", insertedChar.getIsItalic());
                diff.put("underline", insertedChar.getUnderLine());
                diff.put("flagDelete", insertedChar.isFlagDelete());
                diff.put("siteID", insertedChar.getSiteID());
                // diff.put("header", insertedChar.getHeader());
                // Convert the diff to JSON and send it
                String diffJson = objectMapper.writeValueAsString(diff);
                TextMessage diffMessage = new TextMessage(diffJson);
                sendMessage(documentId, diffMessage);
                // System.out.println("Ana geeet Tany hena y3mna " + index + " " + startIndex +
                // " " + endIndex + " "
                // + charValue + " " + id + " " + insertedChar);
                // String content = "content" + sequences.getSequence() + "index" +
                // insertedChar.getIndex();
                // String messageContent = objectMapper.writeValueAsString(sequences.getContent());
                // TextMessage updatedSequenceMessage = new TextMessage(messageContent);
                // System.out.println("final string is " + updatedSequenceMessage);
                // sendMessage(documentId, updatedSequenceMessage);
                System.out.println("Data Saved  " + sequences.getContent());
                session.getAttributes().put("sequence", sequences.getContent());
                session.getAttributes().put("documentId", documentId);

            } else if (operationType.equals("deleteCharacter")) {
                int index = (int) operation.get("indexStart");
                String id = (String) operation.get("id");
                List<CHAR> relatives = sequences.getRelativeIndex(index);
                String id2 = relatives.get(1).getId();
                sequences.delete(id2);
                System.out.println("Ana B3ml Delete ya 3mn " + index + "  " + id2);

                // Create a diff for the deleted character
                Map<String, Object> diff = new HashMap<>();
                diff.put("operationType", "deleteCharacter");
                diff.put("id", id);
                diff.put("index", index);

                // Convert the diff to JSON and send it
                String diffJson = objectMapper.writeValueAsString(diff);
                TextMessage diffMessage = new TextMessage(diffJson);
                sendMessage(documentId, diffMessage);
                session.getAttributes().put("sequence", sequences.getContent());
                session.getAttributes().put("documentId", documentId);
            }
        } else {
            String messageContent = objectMapper.writeValueAsString(sequences.getContent());
            TextMessage updatedSequenceMessage = new TextMessage(messageContent);
            sendMessage(documentId, updatedSequenceMessage);
            sequences.getContent().sort(Comparator.comparingDouble(CHAR::getIndex));
            session.getAttributes().put("sequence", sequences.getContent());
            session.getAttributes().put("documentId", documentId);
        }

    }

    private void sendMessage(String roomId, TextMessage message) {
        // synchronized (lock) {
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
        // }
    }

    private String getDocumentId(TextMessage message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(message.getPayload(), Map.class);
        System.out.println("Document ID: " + map.get("documentId"));
        return map.get("documentId");
    }

    private void saveDocument(String documentId, List<CHAR> content) {
        System.out.println("Saving document");
        documentService.getDocumentById(documentId).ifPresent(document -> {
            document.setContent(content);
            documentService.updateDocument(document);
        });
    }

}