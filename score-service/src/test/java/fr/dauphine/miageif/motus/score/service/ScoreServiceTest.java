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
        // gagne 3/6, 45 s, 6 lettres : 100 + 60 + 41 + 15 = 216
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, 45, null));
        assertThat(r.score()).isEqualTo(216);
    }

    @Test
    void partiePerdueDonneScoreZero() {
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, false, 6, 6, 6, 30, null));
        assertThat(r.score()).isZero();
    }

    @Test
    void maxAttemptsEtDureeInvalidesUtilisentLesDefauts() {
        // maxAttempts=0 -> defaut 6 ; durationSeconds=0 -> stocke 0 ; score 100+60+50+15 = 225
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 0, 6, 0, null));
        assertThat(r.maxAttempts()).isEqualTo(6);
        assertThat(r.durationSeconds()).isZero();
        assertThat(r.score()).isEqualTo(225);
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
        assertThat(s.averageScore()).isZero();
    }

    @Test
    void statsCalculeTotalMeilleurEtMoyenneDeScore() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null));  // 175
        service.record(new GameResultRequest(2L, 1L, false, 6, 6, 6, null, null)); // 0

        PlayerStats s = service.playerStats(1L);
        assertThat(s.gamesPlayed()).isEqualTo(2);
        assertThat(s.wins()).isEqualTo(1);
        assertThat(s.losses()).isEqualTo(1);
        assertThat(s.averageAttempts()).isEqualTo(4.5); // (3 + 6) / 2
        assertThat(s.totalScore()).isEqualTo(175);
        assertThat(s.bestScore()).isEqualTo(175);
        assertThat(s.averageScore()).isEqualTo(87.5); // 175 / 2
    }

    @Test
    void rankingTrieParPointsAvecEcartAuSuivant() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null)); // joueur 1 : 175
        service.record(new GameResultRequest(2L, 1L, true, 5, 6, 6, null, null)); // joueur 1 : 135 -> total 310
        service.record(new GameResultRequest(3L, 2L, true, 2, 6, 6, null, null)); // joueur 2 : 195

        List<RankingEntry> r = service.ranking();
        assertThat(r).hasSize(2);
        assertThat(r.get(0).playerId()).isEqualTo(1L);
        assertThat(r.get(0).totalScore()).isEqualTo(310);
        assertThat(r.get(0).pointsToNext()).isZero(); // le 1er n'a personne devant
        assertThat(r.get(1).playerId()).isEqualTo(2L);
        assertThat(r.get(1).totalScore()).isEqualTo(195);
        assertThat(r.get(1).pointsToNext()).isEqualTo(115); // 310 - 195
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
