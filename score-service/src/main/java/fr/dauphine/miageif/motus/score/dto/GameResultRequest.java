package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Corps JSON envoye par game-service a la fin d'une partie :
// { "gameId": 12, "playerId": 1, "won": true, "attempts": 4, "wordLength": 6 }
// finishedAt est optionnel (par defaut : maintenant).
public record GameResultRequest(
        Long gameId,
        Long playerId,
        boolean won,
        int attempts,
        int wordLength,
        LocalDateTime finishedAt) {
}
