package com.motus.game.dto;

import java.util.List;

public class AttemptResponse {

    private Integer attemptNumber;
    private String word;
    private List<LetterResult> result;

    public AttemptResponse() {
    }

    public AttemptResponse(Integer attemptNumber, String word, List<LetterResult> result) {
        this.attemptNumber = attemptNumber;
        this.word = word;
        this.result = result;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<LetterResult> getResult() {
        return result;
    }

    public void setResult(List<LetterResult> result) {
        this.result = result;
    }
}
