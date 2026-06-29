package fr.dauphine.miageif.motus.dict.exception;

// Levee quand aucun mot ne correspond a la demande (ex : aucune entree de longueur N).
public class WordNotFoundException extends RuntimeException {

    public WordNotFoundException(String message) {
        super(message);
    }
}
