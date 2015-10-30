package com.duanze.gasst.parser;

import java.util.ArrayList;

/**
 * Created by Duanze on 2015/10/30.
 */
public class GNoteDataParser {
    private GNoteMemo state;

    private static final int MAX_CAPACITY = 30;
    private ArrayList<GNoteMemo> lastMemo;
    private ArrayList<GNoteMemo> nextMemo;

    public GNoteDataParser(GNoteMemo gNoteMemo) {
        state = gNoteMemo;
        initMemoList();
    }

    private void initMemoList() {
        lastMemo = new ArrayList<>(MAX_CAPACITY);
        nextMemo = new ArrayList<>(MAX_CAPACITY);
    }

    public GNoteMemo createMemo() {
        return state;
    }

    public void setState(GNoteMemo gNoteMemo) {
        if (null == gNoteMemo) return;
        this.state = gNoteMemo;
    }

    public GNoteMemo getState() {
        return state;
    }

    public void newState(GNoteMemo state) {
        push(lastMemo);
        nextMemo.clear();
        this.state = state;
    }

    private void push(ArrayList<GNoteMemo> lastMemo) {
        checkMaxCapacity(lastMemo);
        lastMemo.add(createMemo());
    }

    private GNoteMemo pop(ArrayList<GNoteMemo> list) {
        if (list.size() > 0) {
            int lastIndex = list.size() - 1;
            GNoteMemo newMemo = list.get(lastIndex);
            list.remove(lastIndex);
            return newMemo;
        }
        return null;
    }

    private void checkMaxCapacity(ArrayList<GNoteMemo> list) {
        if (list.size() >= MAX_CAPACITY) {
            list.remove(0);
        }
    }

    public void undo() {
        GNoteMemo lastState = pop(lastMemo);
        if (null != lastState) {
            push(nextMemo);
            setState(lastState);
        }
    }


    public void redo() {
        GNoteMemo nextState = pop(nextMemo);
        if (null != nextState) {
            push(lastMemo);
            setState(nextState);
        }
    }

    public int lastSize() {
        return lastMemo.size();
    }

    public int nextSize() {
        return nextMemo.size();
    }
}
