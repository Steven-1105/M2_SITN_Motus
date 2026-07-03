package fr.dauphine.miageif.motus.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GameCreateRequest {

    @NotNull(message = "playerId is required")
    private Long playerId;

    @NotNull(message = "wordLength is required")
    @Min(value = 3, message = "wordLength must be at least 3")
    @Max(value = 12, message = "wordLength must be at most 12")
    private Integer wordLength;

    @NotNull(message = "maxAttempts is required")
    @Min(value = 1, message = "maxAttempts must be at least 1")
    private Integer maxAttempts;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
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
}
