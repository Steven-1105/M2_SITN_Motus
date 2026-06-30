package fr.dauphine.miageif.motus.game.dto;

public class ScoreResultRequest {

    private Long gameId;
    private Long playerId;
    private boolean won;
    private Integer attempts;
    private Integer maxAttempts;
    private Integer wordLength;
    private Long durationSeconds;

    public ScoreResultRequest() {
    }

    public ScoreResultRequest(Long gameId, Long playerId, boolean won, Integer attempts,
                               Integer maxAttempts, Integer wordLength, Long durationSeconds) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.won = won;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.wordLength = wordLength;
        this.durationSeconds = durationSeconds;
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

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
