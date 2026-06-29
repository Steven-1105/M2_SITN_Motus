package fr.dauphine.miageif.motus.dict.controller;

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
        assertThat(body.word()).matches("[A-Z]{6}");
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
        assertThat(body.valid()).isTrue();
    }

    @Test
    void validateMotInconnuRetourneFalse() {
        ValidResponse body = rest.postForObject(url("/words/validate"), new WordRequest("ZZZZZZ"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.valid()).isFalse();
    }

    @Test
    void existsEstInsensibleALaCasse() {
        ValidResponse body = rest.getForObject(url("/words/exists?word=maison"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.valid()).isTrue();
    }

    @Test
    void randomSansLongueurUtilise6ParDefaut() {
        WordResponse body = rest.getForObject(url("/words/random"), WordResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.word()).matches("[A-Z]{6}");
    }

    @Test
    void validateSansMotRetourneFalse() {
        // Corps JSON vide {} -> word == null -> valid:false (branche de garde)
        ValidResponse body = rest.postForObject(url("/words/validate"), java.util.Map.of(), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.valid()).isFalse();
    }

    @Test
    void existsMotInconnuRetourneFalse() {
        ValidResponse body = rest.getForObject(url("/words/exists?word=zzzzzz"), ValidResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.valid()).isFalse();
    }
}
