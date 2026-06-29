package fr.dauphine.miageif.motus.score.controller;

import fr.dauphine.miageif.motus.score.dto.PlayerStats;
import fr.dauphine.miageif.motus.score.dto.RankingEntry;
import fr.dauphine.miageif.motus.score.repository.GameResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

// Teste les endpoints en HTTP reel (comme le fera game-service / l'admin). Base H2.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScoreControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GameResultRepository repository;

    private final RestTemplate rest = new RestTemplate();

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> result(long gameId, long playerId, boolean won, int attempts, int wordLength) {
        Map<String, Object> m = new HashMap<>();
        m.put("gameId", gameId);
        m.put("playerId", playerId);
        m.put("won", won);
        m.put("attempts", attempts);
        m.put("wordLength", wordLength);
        return m;
    }

    @Test
    void postResultRetourne201EtPersiste() {
        ResponseEntity<String> r =
                rest.postForEntity(url("/scores/results"), result(1, 1, true, 3, 6), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody()).contains("\"playerId\":1").contains("\"won\":true");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void postResultEstIdempotentParGameId() {
        rest.postForEntity(url("/scores/results"), result(1, 1, true, 3, 6), String.class);
        rest.postForEntity(url("/scores/results"), result(1, 1, false, 5, 6), String.class); // meme gameId

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void postResultSansPlayerIdRetourne400() {
        Map<String, Object> bad = new HashMap<>();
        bad.put("gameId", 1);
        bad.put("won", true);
        bad.put("attempts", 3);
        bad.put("wordLength", 6); // playerId absent

        try {
            rest.postForEntity(url("/scores/results"), bad, String.class);
            fail("Une erreur 400 etait attendue (playerId manquant)");
        } catch (HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("\"status\":400");
        }
    }

    @Test
    void rankingEstTrieParVictoires() {
        rest.postForEntity(url("/scores/results"), result(1, 1, true, 3, 6), String.class);
        rest.postForEntity(url("/scores/results"), result(2, 1, true, 4, 6), String.class);
        rest.postForEntity(url("/scores/results"), result(3, 2, true, 2, 6), String.class);

        RankingEntry[] ranking = rest.getForObject(url("/scores/ranking"), RankingEntry[].class);
        assertThat(ranking).hasSize(2);
        assertThat(ranking[0].playerId()).isEqualTo(1L); // 2 victoires
        assertThat(ranking[0].wins()).isEqualTo(2);
        assertThat(ranking[1].playerId()).isEqualTo(2L); // 1 victoire
    }

    @Test
    void statsJoueurViaHttp() {
        rest.postForEntity(url("/scores/results"), result(1, 1, true, 3, 6), String.class);
        rest.postForEntity(url("/scores/results"), result(2, 1, false, 5, 6), String.class);

        PlayerStats stats = rest.getForObject(url("/scores/players/1"), PlayerStats.class);
        assertThat(stats.gamesPlayed()).isEqualTo(2);
        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.losses()).isEqualTo(1);
    }

    @Test
    void gamesFiltreParJoueur() {
        rest.postForEntity(url("/scores/results"), result(1, 1, true, 3, 6), String.class);
        rest.postForEntity(url("/scores/results"), result(2, 2, true, 3, 6), String.class);

        List<?> tous = rest.getForObject(url("/scores/games"), List.class);
        assertThat(tous).hasSize(2);

        List<?> joueur1 = rest.getForObject(url("/scores/games?playerId=1"), List.class);
        assertThat(joueur1).hasSize(1);
    }
}
