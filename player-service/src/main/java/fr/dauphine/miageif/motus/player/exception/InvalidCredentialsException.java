package fr.dauphine.miageif.motus.player.exception;

// Levee lorsque le pseudo/email OU le mot de passe est incorrect a la connexion.
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
