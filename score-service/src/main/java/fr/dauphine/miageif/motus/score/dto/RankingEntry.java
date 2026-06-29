package fr.dauphine.miageif.motus.score.dto;

// Une ligne du classement global : GET /scores/ranking
// Le classement est trie par totalScore (total des points) decroissant.
public record RankingEntry(
        Long playerId,
        int gamesPlayed,
        int wins,
        int losses,
        double winRate,
        double averageAttempts,
        int totalScore) {
}
