package com.motus.game.dto;

import com.motus.game.entity.Game;
import com.motus.game.entity.GameStatus;

import java.util.List;

public class GameResponse {

    private Long id;
    private Long playerId;
    private GameStatus statut;
    private String motMystere;
    private Integer wordLength;
    private Integer maxAttempts;
    private Integer attemptsUsed;
    private List<AttemptResponse> attempts;

    public GameResponse() {
    }

    public static GameResponse fromEntity(Game game, List<AttemptResponse> attempts) {
        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setPlayerId(game.getPlayerId());
        response.setStatut(game.getStatut());
        response.setWordLength(game.getWordLength());
        response.setMaxAttempts(game.getMaxAttempts());
        response.setAttemptsUsed(game.getAttemptsUsed());
        response.setAttempts(attempts);

        boolean finished = game.getStatut() != GameStatus.EN_COURS;
        response.setMotMystere(finished ? game.getMotMystere() : "*".repeat(game.getWordLength()));

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public GameStatus getStatut() {
        return statut;
    }

    public void setStatut(GameStatus statut) {
        this.statut = statut;
    }

    public String getMotMystere() {
        return motMystere;
    }

    public void setMotMystere(String motMystere) {
        this.motMystere = motMystere;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(Integer attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public List<AttemptResponse> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<AttemptResponse> attempts) {
        this.attempts = attempts;
    }
}
