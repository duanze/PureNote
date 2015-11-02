package com.duanze.gasst.memento;

import java.util.ArrayList;

/**
 * Created by Duanze on 2015/10/30.
 */
public class Originator {
    private Memo state;

    private static final int MAX_CAPACITY = 30;
    private ArrayList<Memo> lastMemo;
    private ArrayList<Memo> nextMemo;

    public Originator(Memo memo) {
        state = memo;
        initMemoList();
    }

    private void initMemoList() {
        lastMemo = new ArrayList<>(MAX_CAPACITY);
        nextMemo = new ArrayList<>(MAX_CAPACITY);
    }

    public Memo createMemo() {
        return state;
    }

    public void setState(Memo memo) {
        if (null == memo) return;
        this.state = memo;
    }

    public Memo getState() {
        return state;
    }

    public void newState(Memo state) {
        push(lastMemo);
        nextMemo.clear();
        this.state = state;
    }

    private void push(ArrayList<Memo> lastMemo) {
        checkMaxCapacity(lastMemo);
        lastMemo.add(createMemo());
    }

    private Memo pop(ArrayList<Memo> list) {
        if (list.size() > 0) {
            int lastIndex = list.size() - 1;
            Memo newMemo = list.get(lastIndex);
            list.remove(lastIndex);
            return newMemo;
        }
        return null;
    }

    private void checkMaxCapacity(ArrayList<Memo> list) {
        if (list.size() >= MAX_CAPACITY) {
            list.remove(0);
        }
    }

    public void undo() {
        Memo lastState = pop(lastMemo);
        if (null != lastState) {
            push(nextMemo);
            setState(lastState);
        }
    }


    public void redo() {
        Memo nextState = pop(nextMemo);
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
