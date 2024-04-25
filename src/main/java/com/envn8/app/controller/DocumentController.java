package com.envn8.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.envn8.app.models.Documents;
import com.envn8.app.models.User;
import com.envn8.app.service.DocumentService;
import com.envn8.app.service.userService;

// import java.util.List;
// import java.util.Map;
import java.util.Optional;

@RestController
public class DocumentController {

    @Autowired
    private DocumentService documentService;
    private userService userService;

    @PostMapping("/documents")
    public ResponseEntity<Documents> createDocument(@RequestBody DocumentRequest documentRequest,
            @RequestHeader("userId") String userId) {
        User user = userService.getUserById(userId); // Get the user who is creating the document
        Documents document = new Documents();
        document.setTitle(documentRequest.getTitle());
        document.setContent(documentRequest.getContent());
        document.setOwner(user); // Set the owner of the document to the user
        Documents savedDocument = documentService.createDocument(document);
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    // Open a document
    @GetMapping("/documents/{id}")
    public ResponseEntity<Documents> viewDocument(@PathVariable String id, @RequestHeader("userId") String userId) {
        Optional<Documents> documentOptional = documentService.getDocumentById(id);
        if (documentOptional.isPresent()) {
            Documents document = documentOptional.get();
            if (document.getType().equals("public") || document.getOwner().getId().equals(userId)
                    || document.getSharedWith().contains(userService.getUserById(userId))) {
                return new ResponseEntity<>(documentOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Rename a document
    @PutMapping("/documents/{id}/rename")
    public ResponseEntity<Documents> renameDocument(@PathVariable String id,
            @RequestBody DocumentRequest documentRequest, @RequestHeader("userId") String userId) {
        Optional<Documents> documentOptional = documentService.getDocumentById(id); // Get the document by id
        if (documentOptional.isPresent()) { // If the document exists
            Documents document = documentOptional.get();
            String allowedonly = "editor";
            if (document.getOwner().getId().equals(userId)
                    || allowedonly.equals(document.getPermissions().get(userId))) {
                document.setTitle(documentRequest.getTitle()); // Set the new title
                Documents updatedDocument = documentService.renameDocument(document);
                return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/documents/{id}/edit")
    public ResponseEntity<Documents> editDocument(@PathVariable String id, @RequestBody DocumentRequest documentRequest,
            @RequestHeader("userId") String userId) {
        Optional<Documents> documentOptional = documentService.getDocumentById(id); // Get the document by id
        if (documentOptional.isPresent()) { // If the document exists
            Documents document = documentOptional.get();
            String allowedonly = "editor";
            if (document.getOwner().getId().equals(userId)
                    || allowedonly.equals(document.getPermissions().get(userId))) {
                document.setContent(documentRequest.getContent()); // Set the new content
                Documents updatedDocument = documentService.updateDocument(document);
                // TODO: Notify all clients viewing the document that it has been updated
                // web socket to be used here
                return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a document
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id, @RequestHeader("userId") String userId) {
        Optional<Documents> documentOptional = documentService.getDocumentById(id);
        if (documentOptional.isPresent()) {
            Documents document = documentOptional.get();
            if (document.getOwner().getId().equals(userId)) {
                documentService.deleteDocument(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/documents/{id}/share")
    public ResponseEntity<Documents> shareDocument(@PathVariable String id, @RequestBody ShareRequest shareRequest) {
        Optional<Documents> documentOptional = documentService.getDocumentById(id);
        if (documentOptional.isPresent()) {
            Documents document = documentOptional.get();
            User userToShareWith = userService.getUserByUsername(shareRequest.getUsername());
            document.getSharedWith().add(userToShareWith);
            document.getPermissions().put(userToShareWith.getId(), shareRequest.getPermission());
            // to save new sharedWith and permissions
            Documents updatedDocument = documentService.updateDocument(document); 
            return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    static class DocumentRequest {
        private String content;
        private String title;
        private String type;
        // Getters and setters

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    static class ShareRequest {
        private String username; // This represents the user that the document is shared with
        private String permission; // This represents the permission of the shared user

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
}