package fr.dauphine.miageif.motus.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Resultat d'une partie terminee, envoye par game-service (modele "push").
// score-service le persiste pour calculer classement et statistiques.
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

    @Column(name = "word_length", nullable = false)
    private int wordLength;

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

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
