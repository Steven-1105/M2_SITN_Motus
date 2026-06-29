package fr.dauphine.miageif.motus.score.service;

import fr.dauphine.miageif.motus.score.dto.GameResultRequest;
import fr.dauphine.miageif.motus.score.dto.GameResultResponse;
import fr.dauphine.miageif.motus.score.dto.PlayerStats;
import fr.dauphine.miageif.motus.score.dto.RankingEntry;
import fr.dauphine.miageif.motus.score.entity.GameResult;
import fr.dauphine.miageif.motus.score.repository.GameResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ScoreService {

    private final GameResultRepository repository;

    public ScoreService(GameResultRepository repository) {
        this.repository = repository;
    }

    // Enregistre (ou met a jour) le resultat d'une partie. Idempotent par gameId :
    // si game-service renvoie le meme resultat, on ne cree pas de doublon.
    // Le score est calcule ici (score-service = autorite des scores).
    public GameResultResponse record(GameResultRequest req) {
        if (req.gameId() == null || req.playerId() == null) {
            throw new IllegalArgumentException("gameId et playerId sont obligatoires");
        }
        int maxAttempts = (req.maxAttempts() != null && req.maxAttempts() > 0)
                ? req.maxAttempts() : ScoreCalculator.MAX_ESSAIS_DEFAUT;
        int durationSeconds = (req.durationSeconds() != null && req.durationSeconds() > 0)
                ? req.durationSeconds() : 0;
        int score = ScoreCalculator.compute(req.won(), req.attempts(), maxAttempts,
                req.wordLength(), req.durationSeconds());

        GameResult gr = repository.findByGameId(req.gameId()).orElseGet(GameResult::new);
        gr.setGameId(req.gameId());
        gr.setPlayerId(req.playerId());
        gr.setWon(req.won());
        gr.setAttempts(req.attempts());
        gr.setMaxAttempts(maxAttempts);
        gr.setWordLength(req.wordLength());
        gr.setDurationSeconds(durationSeconds);
        gr.setScore(score);
        gr.setFinishedAt(req.finishedAt() != null ? req.finishedAt() : LocalDateTime.now());
        return toResponse(repository.save(gr));
    }

    // Classement global : trie par total de points (desc), puis victoires (desc), puis joueur.
    public List<RankingEntry> ranking() {
        Map<Long, List<GameResult>> byPlayer = repository.findAll().stream()
                .collect(Collectors.groupingBy(GameResult::getPlayerId));
        return byPlayer.entrySet().stream()
                .map(e -> {
                    Stats s = computeStats(e.getValue());
                    return new RankingEntry(e.getKey(), s.games(), s.wins(), s.losses(),
                            s.winRate(), s.avgAttempts(), s.totalScore());
                })
                .sorted(Comparator.comparingInt(RankingEntry::totalScore).reversed()
                        .thenComparing(Comparator.comparingInt(RankingEntry::wins).reversed())
                        .thenComparing(RankingEntry::playerId))
                .toList();
    }

    // Statistiques d'un joueur (zeros s'il n'a aucune partie enregistree).
    public PlayerStats playerStats(Long playerId) {
        Stats s = computeStats(repository.findByPlayerId(playerId));
        return new PlayerStats(playerId, s.games(), s.wins(), s.losses(),
                s.winRate(), s.avgAttempts(), s.totalScore(), s.bestScore());
    }

    // Liste des parties (admin) avec filtres optionnels : joueur, plage de dates.
    public List<GameResultResponse> games(Long playerId, LocalDate from, LocalDate to) {
        Stream<GameResult> stream =
                (playerId != null ? repository.findByPlayerId(playerId) : repository.findAll()).stream();
        if (from != null) {
            stream = stream.filter(g -> !g.getFinishedAt().toLocalDate().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(g -> !g.getFinishedAt().toLocalDate().isAfter(to));
        }
        return stream
                .sorted(Comparator.comparing(GameResult::getFinishedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    // --- helpers ---

    private record Stats(int games, int wins, int losses, double winRate, double avgAttempts,
                         int totalScore, int bestScore) {
    }

    private Stats computeStats(List<GameResult> list) {
        int games = list.size();
        int wins = (int) list.stream().filter(GameResult::isWon).count();
        int losses = games - wins;
        double winRate = games > 0 ? round((double) wins / games) : 0.0;
        double avgAttempts = games > 0
                ? round(list.stream().mapToInt(GameResult::getAttempts).average().orElse(0))
                : 0.0;
        int totalScore = list.stream().mapToInt(GameResult::getScore).sum();
        int bestScore = list.stream().mapToInt(GameResult::getScore).max().orElse(0);
        return new Stats(games, wins, losses, winRate, avgAttempts, totalScore, bestScore);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private GameResultResponse toResponse(GameResult g) {
        return new GameResultResponse(g.getId(), g.getGameId(), g.getPlayerId(),
                g.isWon(), g.getAttempts(), g.getMaxAttempts(), g.getWordLength(),
                g.getDurationSeconds(), g.getScore(), g.getFinishedAt());
    }
}
