package fr.dauphine.miageif.motus.score.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire de la formule : score = (100 + bonus essais + bonus temps) * (longueur - 2).
class ScoreCalculatorTest {

    @Test
    void gagneViteAvecPeuDEssaisEtMotLong() {
        // gagne 1/6, 20 s, 8 lettres : base = 100 + (6-1)*20=100 + (50-20/5=46) = 246 ; x(8-2)=6 -> 1476
        assertThat(ScoreCalculator.compute(true, 1, 6, 8, 20)).isEqualTo(1476);
    }

    @Test
    void partiePerdueVautZero() {
        assertThat(ScoreCalculator.compute(false, 1, 6, 8, 20)).isZero();
    }

    @Test
    void sansDureeAucunBonusTemps() {
        // gagne 3/6, pas de duree, 6 lettres : base 160 ; x(6-2)=4 -> 640
        assertThat(ScoreCalculator.compute(true, 3, 6, 6, null)).isEqualTo(640);
    }

    @Test
    void tempsLongAnnuleLeBonusTemps() {
        // 300 s -> 0 ; gagne 6/6, 6 lettres : base 100 ; x4 -> 400
        assertThat(ScoreCalculator.compute(true, 6, 6, 6, 300)).isEqualTo(400);
    }

    @Test
    void maxAttemptsNullUtiliseLaValeurParDefaut() {
        // max null -> 6 ; gagne 3, 6 lettres : base 160 ; x4 -> 640
        assertThat(ScoreCalculator.compute(true, 3, null, 6, null)).isEqualTo(640);
    }

    @Test
    void motDe5LettresMultiplieParTrois() {
        // 5 lettres -> x(5-2)=3 ; gagne 3/6, pas de duree : base 160 x3 -> 480
        assertThat(ScoreCalculator.compute(true, 3, 6, 5, null)).isEqualTo(480);
    }

    @Test
    void laLongueurMultiplieToutLeScore() {
        // base isolee = 100 (gagne en max essais, sans duree) ; score = 100 * (longueur-2)
        assertThat(ScoreCalculator.compute(true, 4, 4, 4, null)).isEqualTo(200); // x2
        assertThat(ScoreCalculator.compute(true, 5, 5, 5, null)).isEqualTo(300); // x3
        assertThat(ScoreCalculator.compute(true, 6, 6, 6, null)).isEqualTo(400); // x4
        assertThat(ScoreCalculator.compute(true, 9, 9, 9, null)).isEqualTo(700); // x7
    }

    @Test
    void maxAttemptsInvalideRetombeSurLeDefaut() {
        // maxAttempts=0 -> 6 ; duree=0 -> bonus temps 50 ; gagne 3, 6 lettres : base 210 x4 -> 840
        assertThat(ScoreCalculator.compute(true, 3, 0, 6, 0)).isEqualTo(840);
    }
}
