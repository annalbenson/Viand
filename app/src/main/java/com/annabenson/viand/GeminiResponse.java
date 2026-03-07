package com.annabenson.viand;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiResponse {

    @SerializedName("candidates")
    private List<Candidate> candidates;

    public String getText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate c = candidates.get(0);
            if (c.content != null && c.content.parts != null && !c.content.parts.isEmpty()) {
                return c.content.parts.get(0).text;
            }
        }
        return "";
    }

    public static class Candidate {
        @SerializedName("content")
        private Content content;
    }

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;
    }

    public static class Part {
        @SerializedName("text")
        private String text;
    }
}
