package edu.uob.action;

import java.util.HashSet;
import java.util.LinkedList;

public class GameAction {
    private HashSet<String> triggers;
    private HashSet<String> subjects;
    private LinkedList<String> consumed;
    private LinkedList<String> produced;
    private String narration;

    public GameAction(HashSet<String> triggers, HashSet<String> subjects, LinkedList<String> consumed, LinkedList<String> produced, String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumed = consumed;
        this.produced = produced;
        this.narration = narration;
    }

    public HashSet<String> getTriggers() {
        return this.triggers;
    }
    public HashSet<String> getSubjects() {
        return this.subjects;
    }
    public LinkedList<String> getConsumed() {
        return this.consumed;
    }
    public LinkedList<String> getProduced() {
        return this.produced;
    }
    public String getNarration() {
        return this.narration;
    }
}
