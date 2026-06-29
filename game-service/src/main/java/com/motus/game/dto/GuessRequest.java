package com.motus.game.dto;

import jakarta.validation.constraints.NotBlank;

public class GuessRequest {

    @NotBlank(message = "word is required")
    private String word;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
