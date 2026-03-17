package com.example.probuilder;

public class Conversation {
    private int conversationId;
    private int otherUserId;
    private String otherUserName;
    private String otherUserBusiness;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadCount;
    private int isRead;
    private int lastSenderId;

    public Conversation(int conversationId, int otherUserId, String otherUserName, String otherUserBusiness, String lastMessage, String lastMessageTime, int unreadCount, int isRead, int lastSenderId) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName == null ? "Unknown User" : otherUserName;
        this.otherUserBusiness = otherUserBusiness;
        this.lastMessage = lastMessage == null ? "" : lastMessage;
        this.lastMessageTime = lastMessageTime == null ? "" : lastMessageTime;
        this.unreadCount = unreadCount;
        this.isRead = isRead;
        this.lastSenderId = lastSenderId;
    }

    public int getConversationId() { return conversationId; }
    public int getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public String getOtherUserBusiness() { return otherUserBusiness; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    public int getIsRead() { return isRead; }
    public int getLastSenderId() { return lastSenderId; }
}
