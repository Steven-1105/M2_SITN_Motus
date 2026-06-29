package fr.dauphine.miageif.motus.score.dto;

import java.time.LocalDateTime;

// Corps JSON envoye par game-service a la fin d'une partie :
// { "gameId":12, "playerId":1, "won":true, "attempts":4, "maxAttempts":6,
//   "wordLength":6, "durationSeconds":45 }
// maxAttempts, durationSeconds et finishedAt sont optionnels (defauts raisonnables).
public record GameResultRequest(
        Long gameId,
        Long playerId,
        boolean won,
        int attempts,
        Integer maxAttempts,
        int wordLength,
        Integer durationSeconds,
        LocalDateTime finishedAt) {
}
