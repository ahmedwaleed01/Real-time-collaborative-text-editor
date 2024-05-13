package com.envn8.app.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CharacterSequence {
    private List<CHAR> chars;
    private String siteID;
    private int count;

    public CharacterSequence() {
        this.chars = new ArrayList<>();
        this.chars.add(new CHAR(0, "bof", this.siteID, new Object()));
        this.chars.add(new CHAR(10000, "eof", this.siteID, new Object()));
        this.siteID = UUID.randomUUID().toString();
        this.count = 100;
    }
    public boolean getStartIndex(double index) {
       for(CHAR c : this.chars){
           if(c.getIndex() == index){
               return true;
           }
       }
        return false;
    }
    public double generateIndex(int indexStart, int indexEnd) {

        double diff = (indexEnd - indexStart);
        double index;
        if (diff <= 10) {
            index = indexEnd - diff / 100;
        } else if (diff <= 1000) {
            index = Math.round(indexEnd - diff / 10);
        } else if (diff <= 5000) {
            index = Math.round(indexEnd - diff / 100);
        } else {
            index = Math.round(indexEnd - diff / 1000);
        }
        while (getStartIndex(index))
        {
            index-=0.01;
        }
        return index;
    }

    public CHAR insert(double index, String charValue, Object attributes, String id) {
        List<CHAR> relativeChars = getRelativeIndex(index);
        CHAR charStart = relativeChars.get(0);
        CHAR charEnd = relativeChars.get(1);
        System.out.println("*******Relative characters*********************"+charStart.getIndex()+charEnd.getIndex());
        double newIndex = (charStart.getIndex() + charEnd.getIndex()) / 2;
    
        CHAR charObj = (id != null) ? new CHAR(newIndex, charValue, id, attributes, id) :
                new CHAR(newIndex, charValue, id, attributes);
    
        this.chars.add(charObj);
        this.chars.sort(Comparator.comparingDouble(CHAR::getIndex));
        return charObj;
    }

    public void remoteInsert(CHAR charObj) {
        this.chars.add(charObj);
        this.chars.sort(Comparator.comparingDouble(CHAR::getIndex)
                .thenComparing(CHAR::getSiteID)
                .reversed()); 
    }

    public void delete(String id) {
        for (CHAR c : this.chars) {
            if (c.getId().equals(id)) {
                c.setFlagDelete(true);
                break;
            }
        }
    }
    public String getSequence() {
        StringBuilder seq = new StringBuilder();
        for (CHAR c : this.chars) {
            if (!c.isFlagDelete() && !"bof".equals(c.getChar()) && !"eof".equals(c.getChar())) {
                seq.append(c.getChar());
            }
        }
        return seq.toString();
    }


    public List<CHAR> getRelativeIndex(double index) {
        List<CHAR> result = new ArrayList<>();
        int aliveIndex = 0;
        boolean itemsFound = false;
        CHAR charStart = null;
        CHAR charEnd = null;

        for (CHAR c : this.chars) {
            if (!c.isFlagDelete()) {
                if (aliveIndex > index) {
                    charEnd = c;
                    itemsFound = true;
                } else {
                    charStart = c;
                }
                aliveIndex++;
            }
        }

        if (!itemsFound && aliveIndex >= index) {
            charEnd = this.chars.get(this.chars.size() - 1);
            itemsFound = true;
        }
    
        if (index == 0) {
            // A character was inserted at the beginning
            CHAR startChar = new CHAR(0, null, this.chars.get(0).getSiteID(), null, "-0.001");
            result.add(startChar);
            result.add(this.chars.get(0));
        } else if (index == aliveIndex) {
            // A character was inserted at the end
            result.add(this.chars.get(this.chars.size() - 1));
            CHAR endChar = new CHAR(10000000, null, this.chars.get(this.chars.size() - 1).getSiteID(), null, "1000000000");
            result.add(endChar);
        } else if (charStart != null && charEnd != null) {
            // A character was inserted in the middle
            result.add(charStart);
            result.add(charEnd);
        } else {
            throw new IllegalArgumentException("Failed to find relative index");
        }
        
        return result;
    }

    public int getCharRelativeIndex(CHAR charObj) {
        int aliveIndex = 0;
        boolean charFound = false;

        for (CHAR c : this.chars) {
            if (!c.isFlagDelete() && !"bof".equals(c.getChar()) && !"eof".equals(c.getChar())) {
                aliveIndex++;
            }
            if (c.getId().equals(charObj.getId())) {
                if (c.isFlagDelete()) {
                    aliveIndex++;
                }
                charFound = true;
                break;
            }
        }

        if (charFound) {
            return aliveIndex - 1;
        } else {
            throw new IllegalArgumentException("Failed to find relative index");
        }
    }

}
