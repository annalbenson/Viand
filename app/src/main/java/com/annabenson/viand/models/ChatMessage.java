package com.annabenson.viand.models;

public class ChatMessage {

    public enum Type { USER, AI, LOADING }

    private final Type type;
    private String text;

    public ChatMessage(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public Type getType() { return type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
