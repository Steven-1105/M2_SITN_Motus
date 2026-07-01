package fr.dauphine.miageif.motus.score.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire du gestionnaire d'erreurs : branches 400 et 500, format { error, status }.
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentDonne400() {
        ResponseEntity<ErrorResponse> r =
                handler.handleBadRequest(new IllegalArgumentException("playerId manquant"));

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody()).isNotNull();
        assertThat(r.getBody().getStatus()).isEqualTo(400);
        assertThat(r.getBody().getError()).contains("playerId");
    }

    @Test
    void exceptionGeneriqueDonne500() {
        ResponseEntity<ErrorResponse> r = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody()).isNotNull();
        assertThat(r.getBody().getStatus()).isEqualTo(500);
        assertThat(r.getBody().getError()).isEqualTo("boom");
    }
}
