package fr.dauphine.miageif.motus.score.service;

// Calcul du score d'une partie (echelle /10).
//
//   base   = 100  +  (maxEssais - essais) * 20  +  max(0, 50 - secondes/5)
//            victoire     bonus essais               bonus temps
//   score  = base * (longueur - 2)      -> la LONGUEUR multiplie tous les points.
//
// Multiplicateur selon la longueur : 4=x2, 5=x3, 6=x4, 7=x5, 8=x6, 9=x7.
// Un mot plus long double (au minimum) les points gagnes ; plus il est long, plus ca compte.
// Une partie perdue vaut 0. Si la duree n'est pas fournie, le bonus temps vaut 0.
public final class ScoreCalculator {

    public static final int BASE_VICTOIRE = 100;
    public static final int BONUS_PAR_ESSAI_ECONOMISE = 20;
    public static final int BONUS_TEMPS_MAX = 50;
    public static final int SECONDES_PAR_POINT = 5;
    public static final int LONGUEUR_NEUTRE = 2;   // longueur pour laquelle le multiplicateur vaut 0
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

        int base = BASE_VICTOIRE + bonusEssais + bonusTemps;
        int multiplicateur = Math.max(1, wordLength - LONGUEUR_NEUTRE);
        return base * multiplicateur;
    }
}
