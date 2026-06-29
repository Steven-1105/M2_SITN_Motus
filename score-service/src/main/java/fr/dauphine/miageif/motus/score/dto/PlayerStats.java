package fr.dauphine.miageif.motus.score.dto;

// Statistiques d'un joueur : GET /scores/players/{id}
public record PlayerStats(
        Long playerId,
        int gamesPlayed,
        int wins,
        int losses,
        double winRate,
        double averageAttempts,
        int totalScore,
        int bestScore) {
}
