package com.example.tcprototype2.datamodel;

import com.google.firebase.Timestamp;

public class TimeCapsule {
    private String id;
    private String title;
    private String recipient;
    private String sender;
    private String r_name;
    private String s_name;
    private Timestamp created;
    private Timestamp opening;
    private String status;
    private String type;
    private String uri;
    private String filename;
    private int colour;
    private int background;
    private String message;

    public TimeCapsule(){}

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getR_name() {
        return r_name;
    }

    public String getS_name() {
        return s_name;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getOpening() {
        return opening;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getFilename() {
        return filename;
    }

    public int getColour() {
        return colour;
    }

    public int getBackground() {
        return background;
    }

    public String getMessage() {
        return message;
    }
}
