package edu.uob.action;

import java.util.HashSet;

public class GameAction {
    private HashSet<String> triggers;
    private HashSet<String> subjects;
    private HashSet<String> consumed;
    private HashSet<String> produced;
    private String narration;

    public GameAction(HashSet<String> triggers, HashSet<String> subjects, HashSet<String> consumed, HashSet<String> produced, String narration) {
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
    public HashSet<String> getConsumed() {
        return this.consumed;
    }
    public HashSet<String> getProduced() {
        return this.produced;
    }
    public String getNarration() {
        return this.narration;
    }
}
