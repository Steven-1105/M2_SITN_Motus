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

// Tests de la logique metier (calcul de stats / classement / filtres) sur base H2.
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
    void recordEstIdempotentParGameId() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, null));
        service.record(new GameResultRequest(1L, 1L, false, 5, 6, null)); // meme gameId -> mise a jour

        assertThat(repository.count()).isEqualTo(1);
        PlayerStats s = service.playerStats(1L);
        assertThat(s.gamesPlayed()).isEqualTo(1);
        assertThat(s.wins()).isZero(); // la derniere valeur (defaite) ecrase
    }

    @Test
    void recordSansGameIdLeveIllegalArgument() {
        assertThatThrownBy(() -> service.record(new GameResultRequest(null, 1L, true, 3, 6, null)))
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
    }

    @Test
    void statsCalculeVictoiresDefaitesEtMoyenne() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, null));
        service.record(new GameResultRequest(2L, 1L, false, 5, 6, null));

        PlayerStats s = service.playerStats(1L);
        assertThat(s.gamesPlayed()).isEqualTo(2);
        assertThat(s.wins()).isEqualTo(1);
        assertThat(s.losses()).isEqualTo(1);
        assertThat(s.winRate()).isEqualTo(0.5);
        assertThat(s.averageAttempts()).isEqualTo(4.0); // (3 + 5) / 2
    }

    @Test
    void rankingTrieParVictoiresPuisMoyenneDEssais() {
        service.record(new GameResultRequest(1L, 1L, true, 5, 6, null)); // joueur 1 : 1 victoire, 5 essais
        service.record(new GameResultRequest(2L, 2L, true, 2, 6, null)); // joueur 2 : 1 victoire, 2 essais
        service.record(new GameResultRequest(3L, 3L, false, 6, 6, null)); // joueur 3 : 0 victoire

        List<RankingEntry> r = service.ranking();
        assertThat(r).hasSize(3);
        assertThat(r.get(0).playerId()).isEqualTo(2L); // 1 victoire, moins d'essais -> 1er
        assertThat(r.get(1).playerId()).isEqualTo(1L); // 1 victoire, plus d'essais
        assertThat(r.get(2).playerId()).isEqualTo(3L); // 0 victoire -> dernier
    }

    @Test
    void recordSansPlayerIdLeveIllegalArgument() {
        assertThatThrownBy(() -> service.record(new GameResultRequest(1L, null, true, 3, 6, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gamesFiltreParDateDebut() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, LocalDate.of(2026, 6, 15), null);
        assertThat(r).hasSize(1);
        assertThat(r.get(0).gameId()).isEqualTo(2L); // seulement apres le 15
    }

    @Test
    void gamesFiltreParDateFin() {
        service.record(new GameResultRequest(1L, 1L, true, 3, 6, LocalDateTime.of(2026, 6, 1, 10, 0)));
        service.record(new GameResultRequest(2L, 1L, true, 3, 6, LocalDateTime.of(2026, 6, 20, 10, 0)));

        List<GameResultResponse> r = service.games(null, null, LocalDate.of(2026, 6, 15));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).gameId()).isEqualTo(1L); // seulement avant le 15
    }
}
