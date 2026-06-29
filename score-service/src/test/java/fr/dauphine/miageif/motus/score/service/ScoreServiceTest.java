package fr.dauphine.miageif.motus.score.service;

import fr.dauphine.miageif.motus.score.dto.GameResultRequest;
import fr.dauphine.miageif.motus.score.dto.GameResultResponse;
import fr.dauphine.miageif.motus.score.dto.PlayerStats;
import fr.dauphine.miageif.motus.score.dto.RankingEntry;
import fr.dauphine.miageif.motus.score.repository.GameResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Tests de la logique metier (score, stats, classement, filtres) sur base H2.
@SpringBootTest
class ScoreServiceTest {

    @Autowired
    private ScoreService service;

    @Autowired
    private GameResultRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void recordCalculeEtStockeLeScore() {
        // gagne 3/6, 45 s, 6 lettres : 100 + 60 + 41 + 5 = 206
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, 45, null));
        assertThat(r.score()).isEqualTo(206);
    }

    @Test
    void partiePerdueDonneScoreZero() {
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, false, 6, 6, 6, 30, null));
        assertThat(r.score()).isZero();
    }

    @Test
    void maxAttemptsEtDureeInvalidesUtilisentLesDefauts() {
        // maxAttempts=0 -> defaut 6 ; durationSeconds=0 -> stocke 0
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 0, 6, 0, null));
        assertThat(r.maxAttempts()).isEqualTo(6);
        assertThat(r.durationSeconds()).isZero();
        // 100 + (6-3)*20=60 + bonus temps(duree=0 -> 50) + (6-5)*5=5 = 215
        assertThat(r.score()).isEqualTo(215);
    }

    @Test
    void recordEstIdempotentParGameId() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null));
        service.record(new GameResultRequest(1L, 1L, false, 5, 6, 6, null, null)); // meme gameId -> mise a jour

        assertThat(repository.count()).isEqualTo(1);
        PlayerStats s = service.playerStats(1L);
        assertThat(s.gamesPlayed()).isEqualTo(1);
        assertThat(s.wins()).isZero();
        assertThat(s.totalScore()).isZero(); // la defaite ecrase la victoire
    }

    @Test
    void recordSansGameIdLeveIllegalArgument() {
        assertThatThrownBy(() -> service.record(new GameResultRequest(null, 1L, true, 3, 6, 6, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordSansPlayerIdLeveIllegalArgument() {
        assertThatThrownBy(() -> service.record(new GameResultRequest(1L, null, true, 3, 6, 6, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void statsJoueurInconnuDonneDesZeros() {
        PlayerStats s = service.playerStats(999L);
        assertThat(s.gamesPlayed()).isZero();
        assertThat(s.wins()).isZero();
        assertThat(s.losses()).isZero();
        assertThat(s.winRate()).isZero();
        assertThat(s.averageAttempts()).isZero();
        assertThat(s.totalScore()).isZero();
        assertThat(s.bestScore()).isZero();
    }

    @Test
    void statsCalculeTotalEtMeilleurScore() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null));  // 165
        service.record(new GameResultRequest(2L, 1L, false, 6, 6, 6, null, null)); // 0

        PlayerStats s = service.playerStats(1L);
        assertThat(s.gamesPlayed()).isEqualTo(2);
        assertThat(s.wins()).isEqualTo(1);
        assertThat(s.losses()).isEqualTo(1);
        assertThat(s.winRate()).isEqualTo(0.5);
        assertThat(s.averageAttempts()).isEqualTo(4.5); // (3 + 6) / 2
        assertThat(s.totalScore()).isEqualTo(165);
        assertThat(s.bestScore()).isEqualTo(165);
    }

    @Test
    void rankingTrieParTotalDePoints() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null)); // joueur 1 : 165
        service.record(new GameResultRequest(2L, 1L, true, 5, 6, 6, null, null)); // joueur 1 : 125 -> total 290
        service.record(new GameResultRequest(3L, 2L, true, 2, 6, 6, null, null)); // joueur 2 : 185

        List<RankingEntry> r = service.ranking();
        assertThat(r).hasSize(2);
        assertThat(r.get(0).playerId()).isEqualTo(1L);
        assertThat(r.get(0).totalScore()).isEqualTo(290);
        assertThat(r.get(1).playerId()).isEqualTo(2L);
        assertThat(r.get(1).totalScore()).isEqualTo(185);
    }

    @Test
    void gamesFiltreParDateDebut() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, LocalDate.of(2026, 6, 15), null);
        assertThat(r).hasSize(1);
        assertThat(r.get(0).gameId()).isEqualTo(2L);
    }

    @Test
    void gamesFiltreParDateFin() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, null, LocalDate.of(2026, 6, 15));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).gameId()).isEqualTo(1L);
    }
}
