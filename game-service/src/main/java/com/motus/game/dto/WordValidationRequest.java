package com.motus.game.dto;

public class WordValidationRequest {

    private String word;

    public WordValidationRequest() {
    }

    public WordValidationRequest(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
