package com.envn8.app.models;

import java.util.Map;
import java.util.UUID;

public class CHAR {
    private double index;
    private String charValue;
    private boolean FlagDelete;
    private String siteID;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private String header;
    private String id;
    private String firstnullID="0";
    private String lastnullID="100000";
    public CHAR() {

    }

    public CHAR(double index, String charValue, String siteID, Object attributes, String id) {
        this.index = index;
        this.charValue = charValue;
        this.siteID = siteID;
        this.FlagDelete = false;
        if (attributes != null) {
            this.bold = attributes instanceof Map && ((Map) attributes).containsKey("bold")
                    ? (boolean) ((Map) attributes).get("bold")
                    : false;
            this.italic = attributes instanceof Map && ((Map) attributes).containsKey("italic")
                    ? (boolean) ((Map) attributes).get("italic")
                    : false;
            this.underline = attributes instanceof Map && ((Map) attributes).containsKey("underline")
                    ? (boolean) ((Map) attributes).get("underline")
                    : false;
            this.header = attributes instanceof Map && ((Map) attributes).containsKey("header")
                    ? (String) ((Map) attributes).get("header")
                    : null;
        } else {
            this.bold = false;
            this.italic = false;
            this.underline = false;
            this.header = null;
        }
        this.id = id;
    }

    public CHAR(double index, String charValue, String siteID, Object attributes) {
        this.index = index;
        this.charValue = charValue;
        this.siteID = siteID;
        this.FlagDelete = false;
        if (attributes != null) {
            this.bold = attributes instanceof Map && ((Map) attributes).containsKey("bold")
                    ? (boolean) ((Map) attributes).get("bold")
                    : false;
            this.italic = attributes instanceof Map && ((Map) attributes).containsKey("italic")
                    ? (boolean) ((Map) attributes).get("italic")
                    : false;
            this.underline = attributes instanceof Map && ((Map) attributes).containsKey("underline")
                    ? (boolean) ((Map) attributes).get("underline")
                    : false;
            this.header = attributes instanceof Map && ((Map) attributes).containsKey("header")
                    ? (String) ((Map) attributes).get("header")
                    : null;
        } else {
            this.bold = false;
            this.italic = false;
            this.underline = false;
            this.header = null;
        }
        this.id = UUID.randomUUID().toString();
    }

    public void update(Object attributes) {
        if (attributes != null) {
            this.bold = attributes instanceof Map && ((Map) attributes).containsKey("bold")
                    ? (boolean) ((Map) attributes).get("bold")
                    : this.bold;
            this.italic = attributes instanceof Map && ((Map) attributes).containsKey("italic")
                    ? (boolean) ((Map) attributes).get("italic")
                    : this.italic;
            this.underline = attributes instanceof Map && ((Map) attributes).containsKey("underline")
                    ? (boolean) ((Map) attributes).get("underline")
                    : this.underline;
        }
    }

    public String getId() {
        return this.id;
    }

    public String getChar() {
        return this.charValue;
    }

    public double getIndex() {
        return this.index;
    }

    public boolean isFlagDelete() {
        return this.FlagDelete;
    }

    public String getSiteID() {
        return this.siteID;
    }

    public void setFlagDelete(boolean val) {
        this.FlagDelete = val;
    }

    public Object getAttributes() {
        return Map.of("bold", this.bold, "italic", this.italic, "underline", this.underline, "header", this.header);
    }
    public String getFirstnullID() {
        return firstnullID;
    }

    public String getLastnullID() {
        return lastnullID;
    }

}
