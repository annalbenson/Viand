package com.annabenson.viand;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class GeminiRequest {

    @SerializedName("system_instruction")
    private Content systemInstruction;

    @SerializedName("contents")
    private List<Content> contents;

    public GeminiRequest(String systemPrompt, List<Content> contents) {
        this.systemInstruction = new Content(null,
                Collections.singletonList(new Part(systemPrompt)));
        this.contents = contents;
    }

    public static class Content {
        @SerializedName("role")
        private String role; // "user" or "model"

        @SerializedName("parts")
        private List<Part> parts;

        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
