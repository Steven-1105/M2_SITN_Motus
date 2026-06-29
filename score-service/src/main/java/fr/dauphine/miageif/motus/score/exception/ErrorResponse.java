package fr.dauphine.miageif.motus.score.exception;

// Format d'erreur commun convenu dans le binome : { "error": "...", "status": 404 }
public record ErrorResponse(String error, int status) {
}
