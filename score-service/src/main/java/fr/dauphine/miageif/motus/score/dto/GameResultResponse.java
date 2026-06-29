package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Representation d'un resultat de partie (consultation admin / GET /scores/games),
// incluant le score calcule par score-service.
public record GameResultResponse(
        Long id,
        Long gameId,
        Long playerId,
        boolean won,
        int attempts,
        int maxAttempts,
        int wordLength,
        int durationSeconds,
        int score,
        LocalDateTime finishedAt) {
}
