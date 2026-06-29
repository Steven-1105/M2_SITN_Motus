package fr.dauphine.miageif.motus.score.dto;

// Une ligne du classement global : GET /scores/ranking
// - totalScore : total des points du joueur (critere de tri, decroissant)
// - averageScore : points moyens par partie
// - pointsToNext : points qd'il manque pour rattraper le joueur juste au-dessus (0 pour le 1er)
public record RankingEntry(
        Long playerId,
        int gamesPlayed,
        int wins,
        int losses,
        double winRate,
        double averageAttempts,
        int totalScore,
        double averageScore,
        int pointsToNext) {
}
