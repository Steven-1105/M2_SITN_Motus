package fr.dauphine.miageif.motus.game.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_attempts")
public class GameAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String resultPattern;

    public GameAttempt() {
    }

    public GameAttempt(Game game, Integer attemptNumber, String word, String resultPattern) {
        this.game = game;
        this.attemptNumber = attemptNumber;
        this.word = word;
        this.resultPattern = resultPattern;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getResultPattern() {
        return resultPattern;
    }

    public void setResultPattern(String resultPattern) {
        this.resultPattern = resultPattern;
    }
}
