package fr.dauphine.miageif.motus.score.service;

// Calcul du score d'une partie (echelle /10).
//
//   score = 100  +  (maxEssais - essais) * 20  +  max(0, 50 - secondes/5)  +  bonusLongueur
//           base       bonus essais                 bonus temps
//
// bonusLongueur = 20 * 2^(longueur - 5)  -> il DOUBLE a chaque lettre en plus :
//   5 lettres = 20, 6 = 40, 7 = 80, 8 = 160, 9 = 320.
// La longueur (= la difficulte) pese donc beaucoup, de facon geometrique.
// Une partie perdue vaut 0. Si la duree n'est pas fournie, le bonus temps vaut 0.
public final class ScoreCalculator {

    public static final int BASE_VICTOIRE = 100;
    public static final int BONUS_PAR_ESSAI_ECONOMISE = 20;
    public static final int BONUS_TEMPS_MAX = 50;
    public static final int SECONDES_PAR_POINT = 5;
    public static final int BONUS_LONGUEUR_BASE = 20;   // poids d'un mot de 5 lettres (double par lettre)
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

        // Bonus longueur geometrique : il double a chaque lettre au-dela de 5.
        int exposant = Math.min(Math.max(0, wordLength - LONGUEUR_REFERENCE), 10);
        int bonusLongueur = BONUS_LONGUEUR_BASE * (1 << exposant);

        return BASE_VICTOIRE + bonusEssais + bonusTemps + bonusLongueur;
    }
}
