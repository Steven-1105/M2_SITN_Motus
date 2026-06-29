package fr.dauphine.miageif.motus.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Resultat d'une partie terminee, envoye par game-service (modele "push").
// score-service le persiste, calcule le score, et s'en sert pour classement et stats.
@Entity
@Table(name = "game_result")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant de la partie cote game-service (unique : evite les doublons si renvoi).
    @Column(name = "game_id", nullable = false, unique = true)
    private Long gameId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private boolean won;

    // Nombre d'essais utilises dans la partie.
    @Column(nullable = false)
    private int attempts;

    // Nombre d'essais maximum autorise (envoye par game-service).
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "word_length", nullable = false)
    private int wordLength;

    // Duree de la partie en secondes (0 si game-service ne l'a pas envoyee).
    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    // Score (points) calcule par score-service a partir des champs ci-dessus.
    @Column(nullable = false)
    private int score;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;

    public GameResult() {
    }

    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
