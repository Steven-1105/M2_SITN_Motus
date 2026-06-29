package fr.dauphine.miageif.motus.game.dto;

import fr.dauphine.miageif.motus.game.entity.LetterStatus;

public class LetterResult {

    private String lettre;
    private LetterStatus statut;

    public LetterResult() {
    }

    public LetterResult(String lettre, LetterStatus statut) {
        this.lettre = lettre;
        this.statut = statut;
    }

    public String getLettre() {
        return lettre;
    }

    public void setLettre(String lettre) {
        this.lettre = lettre;
    }

    public LetterStatus getStatut() {
        return statut;
    }

    public void setStatut(LetterStatus statut) {
        this.statut = statut;
    }
}
