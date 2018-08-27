package com.example.administrator.dabaggo;

public class LangVO extends Object {

    // VO : Value Object
    int index;
    String lang;
    String content;
    boolean isChecked = false;

    public LangVO() {
    }

    public LangVO(int index, String lang, String content, boolean isChecked) {
        this.index = index;
        this.lang = lang;
        this.content = content;
        this.isChecked = isChecked;
    }
}

