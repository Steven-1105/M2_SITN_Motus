package fr.dauphine.miageif.motus.dict.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire du gestionnaire d'erreurs : verifie les deux branches (404 et 500)
// et le format de reponse { error, status }.
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void wordNotFoundDonne404() {
        ResponseEntity<ErrorResponse> r =
                handler.handleNotFound(new WordNotFoundException("Aucun mot de longueur 99"));

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(r.getBody()).isNotNull();
        assertThat(r.getBody().status()).isEqualTo(404);
        assertThat(r.getBody().error()).contains("longueur 99");
    }

    @Test
    void exceptionGeneriqueDonne500() {
        ResponseEntity<ErrorResponse> r = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody()).isNotNull();
        assertThat(r.getBody().status()).isEqualTo(500);
        assertThat(r.getBody().error()).isEqualTo("boom");
    }
}
