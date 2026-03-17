package com.example.probuilder;

public class Message {
    private int id;
    private int conversationId;
    private int senderId;
    private String message;
    private String messageType;
    private String fileUrl;
    private int isRead;
    private String createdAt;
    private String senderType;
    private int replyToMessageId;
    private String replyToText;
    private String replyToType;
    private boolean isStarred;

    public Message(int id, int conversationId, int senderId, String senderType, String message, String messageType, String fileUrl, int isRead, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.message = message;
        this.messageType = messageType;
        this.fileUrl = fileUrl;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.replyToMessageId = 0;
        this.replyToText = null;
        this.replyToType = null;
        this.isStarred = false;
    }

    public int getId() { return id; }
    public int getConversationId() { return conversationId; }
    public int getSenderId() { return senderId; }
    public String getSenderType() { return senderType; }
    public String getMessage() { return message; }
    public String getMessageType() { return messageType; }
    public String getFileUrl() { return fileUrl; }
    public int getIsRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }
    public int getReplyToMessageId() { return replyToMessageId; }
    public String getReplyToText() { return replyToText; }
    public String getReplyToType() { return replyToType; }
    public boolean isStarred() { return isStarred; }

    public void setIsRead(int isRead) { this.isRead = isRead; }
    public void setId(int id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setReplyToMessageId(int replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }
    public void setReplyToType(String replyToType) { this.replyToType = replyToType; }
    public void setStarred(boolean starred) { this.isStarred = starred; }
}

