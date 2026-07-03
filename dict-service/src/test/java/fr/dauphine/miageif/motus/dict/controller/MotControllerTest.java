package fr.dauphine.miageif.motus.dict.controller;

import fr.dauphine.miageif.motus.dict.dto.LevelInfo;
import fr.dauphine.miageif.motus.dict.dto.ValidResponse;
import fr.dauphine.miageif.motus.dict.dto.WordRequest;
import fr.dauphine.miageif.motus.dict.dto.WordResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

// Demarre un vrai serveur (Tomcat) sur un port aleatoire et appelle les endpoints
// en HTTP, exactement comme le fera game-service. Base H2 de test alimentee par data.sql.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MotControllerTest {

    @LocalServerPort
    private int port;

    private final RestTemplate rest = new RestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void randomRetourneUnMotDeLaBonneLongueur() {
        WordResponse body = rest.getForObject(url("/words/random?length=6"), WordResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.getWord()).matches("[A-Z]{6}");
    }

    @Test
    void randomLongueurInexistanteRetourne404AuFormatErreur() {
        try {
            rest.getForObject(url("/words/random?length=99"), WordResponse.class);
            fail("Une erreur 404 etait attendue pour une longueur inexistante");
        } catch (HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("\"error\"");
            assertThat(ex.getResponseBodyAsString()).contains("\"status\":404");
        }
    }

    @Test
    void validateMotConnuRetourneTrue() {
        ValidResponse body = rest.postForObject(url("/words/validate"), new WordRequest("MAISON"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isTrue();
    }

    @Test
    void validateMotInconnuRetourneFalse() {
        ValidResponse body = rest.postForObject(url("/words/validate"), new WordRequest("ZZZZZZ"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isFalse();
    }

    @Test
    void existsEstInsensibleALaCasse() {
        ValidResponse body = rest.getForObject(url("/words/exists?word=maison"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isTrue();
    }

    @Test
    void randomSansLongueurUtilise6ParDefaut() {
        WordResponse body = rest.getForObject(url("/words/random"), WordResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.getWord()).matches("[A-Z]{6}");
    }

    @Test
    void validateSansMotRetourneFalse() {
        // Corps JSON vide {} -> word == null -> valid:false (branche de garde)
        ValidResponse body = rest.postForObject(url("/words/validate"), java.util.Map.of(), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isFalse();
    }

    @Test
    void existsMotInconnuRetourneFalse() {
        ValidResponse body = rest.getForObject(url("/words/exists?word=zzzzzz"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isFalse();
    }

    @Test
    void lengthsRetourneLesLongueursDisponiblesAvecComptes() {
        LevelInfo[] niveaux = rest.getForObject(url("/words/lengths"), LevelInfo[].class);
        assertThat(niveaux).extracting(LevelInfo::getLength).contains(5, 6, 7, 8);
        assertThat(niveaux).allMatch(n -> n.getCount() > 0);
    }

    @Test
    void randomNePropossJamaisUneFormeConjugueeCommeReponse() {
        // PRIEZ (jeu de test) est une conjugaison : jamais tiree comme mot mystere,
        // meme si elle reste une proposition valide pour le joueur (cf. test suivant).
        for (int i = 0; i < 200; i++) {
            WordResponse body = rest.getForObject(url("/words/random?length=5"), WordResponse.class);
            assertThat(body.getWord()).isNotEqualTo("PRIEZ");
        }
    }

    @Test
    void validateAcceptEncoreLesFormesConjuguesCommeProposition() {
        // Le joueur peut proposer PRIEZ pour eliminer des lettres, meme si ce ne sera
        // jamais le mot a deviner.
        ValidResponse body = rest.postForObject(url("/words/validate"), new WordRequest("PRIEZ"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.isValid()).isTrue();
    }
}
