package fr.dauphine.miageif.motus.score.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire pur de la formule de score (echelle /10).
class ScoreCalculatorTest {

    @Test
    void gagneViteAvecPeuDEssaisEtMotLong() {
        // gagne 1/6, 20 s, 8 lettres : 100 + (6-1)*20=100 + (50-20/5=46) + (8-5)*5=15 = 261
        assertThat(ScoreCalculator.compute(true, 1, 6, 8, 20)).isEqualTo(261);
    }

    @Test
    void partiePerdueVautZero() {
        assertThat(ScoreCalculator.compute(false, 1, 6, 8, 20)).isZero();
    }

    @Test
    void sansDureeAucunBonusTemps() {
        // gagne 3/6, pas de duree, 6 lettres : 100 + 60 + 0 + 5 = 165
        assertThat(ScoreCalculator.compute(true, 3, 6, 6, null)).isEqualTo(165);
    }

    @Test
    void tempsLongAnnuleLeBonusTemps() {
        // 300 s -> 50 - 60 -> 0 ; gagne 6/6, 6 lettres : 100 + 0 + 0 + 5 = 105
        assertThat(ScoreCalculator.compute(true, 6, 6, 6, 300)).isEqualTo(105);
    }

    @Test
    void maxAttemptsNullUtiliseLaValeurParDefaut() {
        // max null -> 6 par defaut ; gagne 3, 6 lettres : 100 + 60 + 0 + 5 = 165
        assertThat(ScoreCalculator.compute(true, 3, null, 6, null)).isEqualTo(165);
    }

    @Test
    void motDeReferenceAucunBonusLongueur() {
        // 5 lettres -> bonus longueur 0 ; gagne 3/6, pas de duree : 100 + 60 + 0 + 0 = 160
        assertThat(ScoreCalculator.compute(true, 3, 6, 5, null)).isEqualTo(160);
    }

    @Test
    void maxAttemptsInvalideRetombeSurLeDefaut() {
        // maxAttempts=0 (invalide) -> 6 ; duree=0 -> bonus temps 50 ; gagne 3, 6 lettres
        // 100 + (6-3)*20=60 + 50 + 5 = 215
        assertThat(ScoreCalculator.compute(true, 3, 0, 6, 0)).isEqualTo(215);
    }
}
