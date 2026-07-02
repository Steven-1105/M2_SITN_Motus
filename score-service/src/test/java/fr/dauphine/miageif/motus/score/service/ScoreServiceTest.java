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
        // gagne 3/6, 45 s, 6 lettres : base 100+60+41=201 ; x(6-2)=4 -> 804
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, 45, null));
        assertThat(r.getScore()).isEqualTo(804);
    }

    @Test
    void partiePerdueDonneScoreZero() {
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, false, 6, 6, 6, 30, null));
        assertThat(r.getScore()).isZero();
    }

    @Test
    void maxAttemptsEtDureeInvalidesUtilisentLesDefauts() {
        // maxAttempts=0 -> defaut 6 ; durationSeconds=0 -> stocke 0 ; base 210 x4 -> 840
        GameResultResponse r = service.record(new GameResultRequest(1L, 1L, true, 3, 0, 6, 0, null));
        assertThat(r.getMaxAttempts()).isEqualTo(6);
        assertThat(r.getDurationSeconds()).isZero();
        assertThat(r.getScore()).isEqualTo(840);
    }

    @Test
    void recordEstIdempotentParGameId() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null));
        service.record(new GameResultRequest(1L, 1L, false, 5, 6, 6, null, null)); // meme gameId -> mise a jour

        assertThat(repository.count()).isEqualTo(1);
        PlayerStats s = service.playerStats(1L);
        assertThat(s.getGamesPlayed()).isEqualTo(1);
        assertThat(s.getWins()).isZero();
        assertThat(s.getTotalScore()).isZero(); // la defaite ecrase la victoire
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
        assertThat(s.getGamesPlayed()).isZero();
        assertThat(s.getWins()).isZero();
        assertThat(s.getLosses()).isZero();
        assertThat(s.getWinRate()).isZero();
        assertThat(s.getAverageAttempts()).isZero();
        assertThat(s.getTotalScore()).isZero();
        assertThat(s.getBestScore()).isZero();
        assertThat(s.getAverageScore()).isZero();
    }

    @Test
    void statsCalculeTotalMeilleurEtMoyenneDeScore() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null));  // 640
        service.record(new GameResultRequest(2L, 1L, false, 6, 6, 6, null, null)); // 0

        PlayerStats s = service.playerStats(1L);
        assertThat(s.getGamesPlayed()).isEqualTo(2);
        assertThat(s.getWins()).isEqualTo(1);
        assertThat(s.getLosses()).isEqualTo(1);
        assertThat(s.getAverageAttempts()).isEqualTo(4.5); // (3 + 6) / 2
        assertThat(s.getTotalScore()).isEqualTo(640);
        assertThat(s.getBestScore()).isEqualTo(640);
        assertThat(s.getAverageScore()).isEqualTo(320.0); // 640 / 2
    }

    @Test
    void rankingTrieParPointsAvecEcartAuSuivant() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, null)); // joueur 1 : 640
        service.record(new GameResultRequest(2L, 1L, true, 5, 6, 6, null, null)); // joueur 1 : 480 -> total 1120
        service.record(new GameResultRequest(3L, 2L, true, 2, 6, 6, null, null)); // joueur 2 : 720

        List<RankingEntry> r = service.ranking();
        assertThat(r).hasSize(2);
        assertThat(r.get(0).getPlayerId()).isEqualTo(1L);
        assertThat(r.get(0).getTotalScore()).isEqualTo(1120);
        assertThat(r.get(0).getPointsToNext()).isZero(); // le 1er n'a personne devant
        assertThat(r.get(1).getPlayerId()).isEqualTo(2L);
        assertThat(r.get(1).getTotalScore()).isEqualTo(720);
        assertThat(r.get(1).getPointsToNext()).isEqualTo(400); // 1120 - 720
    }

    @Test
    void gamesFiltreParDateDebut() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, LocalDate.of(2026, 6, 15), null);
        assertThat(r).hasSize(1);
        assertThat(r.get(0).getGameId()).isEqualTo(2L);
    }

    @Test
    void gamesFiltreParDateFin() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, 6, null, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, null, LocalDate.of(2026, 6, 15));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).getGameId()).isEqualTo(1L);
    }
}
