package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Representation d'un resultat de partie (consultation admin / GET /scores/games).
public record GameResultResponse(
        Long id,
        Long gameId,
        Long playerId,
        boolean won,
        int attempts,
        int wordLength,
        LocalDateTime finishedAt) {
}
