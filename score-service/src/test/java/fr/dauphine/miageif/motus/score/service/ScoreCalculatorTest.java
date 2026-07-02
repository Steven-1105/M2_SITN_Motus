package fr.dauphine.miageif.motus.score.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire pur de la formule de score. Bonus longueur = 20 * 2^(longueur-5) (double par lettre).
class ScoreCalculatorTest {

    @Test
    void gagneViteAvecPeuDEssaisEtMotLong() {
        // gagne 1/6, 20 s, 8 lettres : 100 + (6-1)*20=100 + (50-20/5=46) + 20*2^3=160 = 406
        assertThat(ScoreCalculator.compute(true, 1, 6, 8, 20)).isEqualTo(406);
    }

    @Test
    void partiePerdueVautZero() {
        assertThat(ScoreCalculator.compute(false, 1, 6, 8, 20)).isZero();
    }

    @Test
    void sansDureeAucunBonusTemps() {
        // gagne 3/6, pas de duree, 6 lettres : 100 + 60 + 0 + 20*2^1=40 = 200
        assertThat(ScoreCalculator.compute(true, 3, 6, 6, null)).isEqualTo(200);
    }

    @Test
    void tempsLongAnnuleLeBonusTemps() {
        // 300 s -> 0 ; gagne 6/6, 6 lettres : 100 + 0 + 0 + 40 = 140
        assertThat(ScoreCalculator.compute(true, 6, 6, 6, 300)).isEqualTo(140);
    }

    @Test
    void maxAttemptsNullUtiliseLaValeurParDefaut() {
        // max null -> 6 ; gagne 3, 6 lettres : 100 + 60 + 0 + 40 = 200
        assertThat(ScoreCalculator.compute(true, 3, null, 6, null)).isEqualTo(200);
    }

    @Test
    void motDe5LettresRecoitLeBonusDeBase() {
        // 5 lettres -> bonus longueur de base 20 ; gagne 3/6, pas de duree : 100 + 60 + 0 + 20 = 180
        assertThat(ScoreCalculator.compute(true, 3, 6, 5, null)).isEqualTo(180);
    }

    @Test
    void bonusLongueurDoubleAChaqueLettre() {
        // En isolant (gagne en max essais, sans duree) : score = 100 + bonusLongueur.
        int b5 = ScoreCalculator.compute(true, 5, 5, 5, null) - 100; // 20
        int b6 = ScoreCalculator.compute(true, 6, 6, 6, null) - 100; // 40
        int b7 = ScoreCalculator.compute(true, 7, 7, 7, null) - 100; // 80
        assertThat(b5).isEqualTo(20);
        assertThat(b6).isEqualTo(2 * b5);
        assertThat(b7).isEqualTo(2 * b6);
    }

    @Test
    void maxAttemptsInvalideRetombeSurLeDefaut() {
        // maxAttempts=0 -> 6 ; duree=0 -> bonus temps 50 ; gagne 3, 6 lettres : 100 + 60 + 50 + 40 = 250
        assertThat(ScoreCalculator.compute(true, 3, 0, 6, 0)).isEqualTo(250);
    }
}
