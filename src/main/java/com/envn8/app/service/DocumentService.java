package com.envn8.app.service;

import com.envn8.app.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.envn8.app.models.CRDT;
import com.envn8.app.models.Documents;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // Create
    public Documents createDocument(Documents document) {
        return documentRepository.save(document);
    }

    // Read
    public Optional<Documents> getDocumentById(String id) {
        return documentRepository.findById(id);
    }

    // Update
    public Documents updateDocument(Documents document) {
        // This method now also updates the sharedWith and permissions fields of a
        // document
        return documentRepository.save(document);
    }

    // Delete
    public void deleteDocument(String id) {
        documentRepository.deleteById(id);
    }

    // Rename
    public Documents renameDocument(Documents document) {
        return documentRepository.save(document);
    }

    public String getContent(String documentId) {

        Optional<Documents> document = documentRepository.findById(documentId);
        if (document.isEmpty()) {
            System.err.println("Document not found");
            return null;
        }
        Documents doc = document.get();
        return doc.getContent().stream()
                .map(crdt -> String.valueOf(crdt.getCharacter())) // Convert each character to a string
                .collect(Collectors.joining()); // Join all characters in the document
    }

    public void insertCharacter(String documentId, int position, String beforeId, String afterId, char character) {
        System.out.println("ZEMEELYYYYYYYYYyyyyyyyyyyyyyyy "+documentId+" "+position+" "+" "+beforeId+" "+afterId+" "+character);
        Documents document = documentRepository.findById(documentId).orElseThrow();
        String newId = CRDT.generateIdBetween(beforeId, afterId);
        CRDT newElement = new CRDT(character, newId, beforeId, afterId);
        System.out.println("Yarab"+newElement.getCharacter()+"basha     "+newId);
        document.getContent().add(position, newElement);
        document.getContent().sort(Comparator.comparing(CRDT::getId));

        System.out.println("ETB3 DOCUMENT"+document.getContent());
        documentRepository.save(document);
    }

    public void deleteCharacter(String documentId, String characterId) {
        Optional<Documents> document = documentRepository.findById(documentId);
        if (document.isEmpty()) {
            System.err.println("Document not found");
            return;
        } else {
            System.out.println("Document found");
            Documents doc = document.get();
            doc.getContent().removeIf(crdt -> crdt.getId().equals(characterId));
            documentRepository.save(doc);
        }
    }

}