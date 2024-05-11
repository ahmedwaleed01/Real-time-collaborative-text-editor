package com.envn8.app.models;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.RequiredArgsConstructor;
@Data
@Builder
@NoArgsConstructor
// @RequiredArgsConstructor
@AllArgsConstructor
@Document
public class Documents {
    @Id
    private String id;
    @Field
    private String title;
    @Field
    private List<CRDT> content;
    @Field
    private String type;

    @DBRef
    // @JsonIgnore
    @JsonBackReference
    private User owner;
    
    @DBRef
    // @JsonManagedReference
    @JsonBackReference//this worked
    private List<User> sharedWith; // This represents the users that the document is shared with
    // private Map<String, String> permissions; // This represents the permissions of the shared users
    // key is the user id and value is the permission level

    private Map<String,DocPermissions> permissions;
    // public Documents() {
    // }

    public Documents(String title, List<CRDT> content, String type, User owner) {
        this.title = title;
        this.content = content;
        this.owner = owner;
        this.type = type;
        this.sharedWith = null;
        this.permissions = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newtitle) {
        this.title = newtitle;
    }

    public List<CRDT> getContent() {
        return content;
    }

    public void setContent(List<CRDT> newcontent) {
        this.content = newcontent;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String newtype) {
        this.type = newtype;
    }
    public List<User> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<User> sharedWith) {
        this.sharedWith = sharedWith;
    }
 

    // public Map<String, String> getPermissions() {
    //     return permissions;
    // }

    // public void setPermissions(Map<String, String> permissions) {
    //     this.permissions = permissions;
    // }
}
