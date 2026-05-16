package com.example.myloginapp.data;

public class Message {
    public String sender;
    public String receiver;
    public String content;
    public long timestamp;


    public Message() {
    }

    public Message(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }
}
