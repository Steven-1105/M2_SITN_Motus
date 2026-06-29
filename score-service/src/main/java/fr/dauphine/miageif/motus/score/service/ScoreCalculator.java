package fr.dauphine.miageif.motus.score.service;

// Calcul du score d'une partie (echelle /10).
//
//   score = 100  +  (maxEssais - essais) * 20  +  max(0, 50 - secondes/5)  +  (longueur - 5) * 15
//           base       bonus essais                 bonus temps                  bonus longueur (niveau)
//
// La longueur du mot = la difficulte (niveau) : plus le mot est long, plus ca rapporte.
// Une partie perdue vaut 0. Si la duree n'est pas fournie, le bonus temps vaut 0 (pas de triche).
public final class ScoreCalculator {

    public static final int BASE_VICTOIRE = 100;
    public static final int BONUS_PAR_ESSAI_ECONOMISE = 20;
    public static final int BONUS_TEMPS_MAX = 50;
    public static final int SECONDES_PAR_POINT = 5;
    public static final int BONUS_PAR_LETTRE_SUP = 15;
    public static final int LONGUEUR_REFERENCE = 5;
    public static final int MAX_ESSAIS_DEFAUT = 6;

    private ScoreCalculator() {
    }

    public static int compute(boolean won, int attempts, Integer maxAttempts,
                              int wordLength, Integer durationSeconds) {
        if (!won) {
            return 0;
        }
        int max = (maxAttempts != null && maxAttempts > 0) ? maxAttempts : MAX_ESSAIS_DEFAUT;
        int essais = Math.min(Math.max(attempts, 0), max);
        int bonusEssais = (max - essais) * BONUS_PAR_ESSAI_ECONOMISE;

        int bonusTemps = 0;
        if (durationSeconds != null) {
            int secondes = Math.max(durationSeconds, 0);
            bonusTemps = Math.max(0, BONUS_TEMPS_MAX - secondes / SECONDES_PAR_POINT);
        }

        int bonusLongueur = Math.max(0, wordLength - LONGUEUR_REFERENCE) * BONUS_PAR_LETTRE_SUP;

        return BASE_VICTOIRE + bonusEssais + bonusTemps + bonusLongueur;
    }
}
