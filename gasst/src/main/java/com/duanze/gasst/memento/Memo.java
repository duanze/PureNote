package com.duanze.gasst.memento;

/**
 * Created by Duanze on 2015/10/30.
 */
public class Memo {
    private String content;
    private int selectionEnd;

    public Memo(String content, int selectionEnd) {
        this.content = content;
        this.selectionEnd = selectionEnd;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public void setSelectionEnd(int selectionEnd) {
        this.selectionEnd = selectionEnd;
    }
}
