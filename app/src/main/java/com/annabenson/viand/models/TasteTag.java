package com.annabenson.viand.models;

public class TasteTag {

    private final String tag;
    private final String tagType;
    private final float score;

    public TasteTag(String tag, String tagType, float score) {
        this.tag = tag;
        this.tagType = tagType;
        this.score = score;
    }

    public String getTag() { return tag; }
    public String getTagType() { return tagType; }
    public float getScore() { return score; }
}
