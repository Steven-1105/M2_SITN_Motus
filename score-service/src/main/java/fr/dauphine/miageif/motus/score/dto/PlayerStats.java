package fr.dauphine.miageif.motus.score.dto;

// Statistiques d'un joueur : GET /scores/players/{id}
public class PlayerStats {

    private Long playerId;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private double winRate;
    private double averageAttempts;
    private int totalScore;
    private int bestScore;
    private double averageScore;

    public PlayerStats() {
    }

    public PlayerStats(Long playerId, int gamesPlayed, int wins, int losses, double winRate,
                       double averageAttempts, int totalScore, int bestScore, double averageScore) {
        this.playerId = playerId;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
        this.averageAttempts = averageAttempts;
        this.totalScore = totalScore;
        this.bestScore = bestScore;
        this.averageScore = averageScore;
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

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}
