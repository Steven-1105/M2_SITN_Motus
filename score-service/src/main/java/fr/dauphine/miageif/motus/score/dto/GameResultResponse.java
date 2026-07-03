package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Representation d'un resultat de partie (GET /scores/games), avec le score calcule.
public class GameResultResponse {

    private Long id;
    private Long gameId;
    private Long playerId;
    private boolean won;
    private int attempts;
    private int maxAttempts;
    private int wordLength;
    private int durationSeconds;
    private int score;
    private LocalDateTime finishedAt;

    public GameResultResponse() {
    }

    public GameResultResponse(Long id, Long gameId, Long playerId, boolean won, int attempts,
                              int maxAttempts, int wordLength, int durationSeconds, int score,
                              LocalDateTime finishedAt) {
        this.id = id;
        this.gameId = gameId;
        this.playerId = playerId;
        this.won = won;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.wordLength = wordLength;
        this.durationSeconds = durationSeconds;
        this.score = score;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
