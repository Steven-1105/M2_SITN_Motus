package com.motus.game.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private String motMystere;

    @Column(nullable = false)
    private Integer wordLength;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private Integer attemptsUsed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus statut = GameStatus.EN_COURS;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("attemptNumber ASC")
    private List<GameAttempt> attempts = new ArrayList<>();

    public Game() {
    }

    public Game(Long playerId, String motMystere, Integer wordLength, Integer maxAttempts) {
        this.playerId = playerId;
        this.motMystere = motMystere;
        this.wordLength = wordLength;
        this.maxAttempts = maxAttempts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getMotMystere() {
        return motMystere;
    }

    public void setMotMystere(String motMystere) {
        this.motMystere = motMystere;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(Integer attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public GameStatus getStatut() {
        return statut;
    }

    public void setStatut(GameStatus statut) {
        this.statut = statut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<GameAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<GameAttempt> attempts) {
        this.attempts = attempts;
    }
}
