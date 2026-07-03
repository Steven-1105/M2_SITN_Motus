package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Corps JSON envoye par game-service a la fin d'une partie :
// { "gameId":12, "playerId":1, "won":true, "attempts":4, "maxAttempts":6,
//   "wordLength":6, "durationSeconds":45 }
public class GameResultRequest {

    private Long gameId;
    private Long playerId;
    private boolean won;
    private int attempts;
    private Integer maxAttempts;
    private int wordLength;
    private Integer durationSeconds;
    private LocalDateTime finishedAt;

    public GameResultRequest() {
    }

    public GameResultRequest(Long gameId, Long playerId, boolean won, int attempts,
                             Integer maxAttempts, int wordLength, Integer durationSeconds,
                             LocalDateTime finishedAt) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.won = won;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.wordLength = wordLength;
        this.durationSeconds = durationSeconds;
        this.finishedAt = finishedAt;
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

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
