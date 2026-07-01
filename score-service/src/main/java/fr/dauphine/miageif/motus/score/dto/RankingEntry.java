package fr.dauphine.miageif.motus.score.dto;

// Une ligne du classement global : GET /scores/ranking
// - totalScore : total des points (critere de tri, decroissant)
// - averageScore : points moyens par partie
// - pointsToNext : points qu'il manque pour rattraper le joueur au-dessus (0 pour le 1er)
public class RankingEntry {

    private Long playerId;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private double winRate;
    private double averageAttempts;
    private int totalScore;
    private double averageScore;
    private int pointsToNext;

    public RankingEntry() {
    }

    public RankingEntry(Long playerId, int gamesPlayed, int wins, int losses, double winRate,
                        double averageAttempts, int totalScore, double averageScore, int pointsToNext) {
        this.playerId = playerId;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
        this.averageAttempts = averageAttempts;
        this.totalScore = totalScore;
        this.averageScore = averageScore;
        this.pointsToNext = pointsToNext;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public double getAverageAttempts() {
        return averageAttempts;
    }

    public void setAverageAttempts(double averageAttempts) {
        this.averageAttempts = averageAttempts;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getPointsToNext() {
        return pointsToNext;
    }

    public void setPointsToNext(int pointsToNext) {
        this.pointsToNext = pointsToNext;
    }
}
