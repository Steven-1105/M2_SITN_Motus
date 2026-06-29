package fr.dauphine.miageif.motus.dict.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Test unitaire pur (sans Spring) de l'entite Mot : la longueur est derivee du mot.
class MotTest {

    @Test
    void constructeurCalculeLaLongueur() {
        Mot m = new Mot("MAISON");
        assertThat(m.getMot()).isEqualTo("MAISON");
        assertThat(m.getLongueur()).isEqualTo(6);
        assertThat(m.getId()).isNull();
    }

    @Test
    void setMotMetAJourLaLongueur() {
        Mot m = new Mot();
        m.setMot("TABLE");
        assertThat(m.getLongueur()).isEqualTo(5);
    }

    @Test
    void setMotNullDonneLongueurZero() {
        Mot m = new Mot();
        m.setMot(null);
        assertThat(m.getMot()).isNull();
        assertThat(m.getLongueur()).isZero();
    }
}
