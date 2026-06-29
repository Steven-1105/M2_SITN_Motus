package fr.dauphine.miageif.motus.score.dto;

// Une ligne du classement global : GET /scores/ranking
public record RankingEntry(
        Long playerId,
        int gamesPlayed,
        int wins,
        int losses,
        double winRate,
        double averageAttempts) {
}
