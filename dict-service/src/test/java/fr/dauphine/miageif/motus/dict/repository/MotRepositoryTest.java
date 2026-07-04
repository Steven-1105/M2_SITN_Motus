package fr.dauphine.miageif.motus.dict.repository;

import fr.dauphine.miageif.motus.dict.entity.Mot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifie directement les query methods Spring Data sur la base H2 (alimentee par data.sql).
@SpringBootTest
class MotRepositoryTest {

    @Autowired
    private MotRepository repository;

    @Test
    void findByLongueurNeRetourneQueLaBonneLongueur() {
        List<Mot> mots = repository.findByLongueur(6);
        assertThat(mots).isNotEmpty();
        assertThat(mots).allMatch(m -> m.getLongueur() == 6);
    }

    @Test
    void findByLongueurInexistanteRetourneVide() {
        assertThat(repository.findByLongueur(99)).isEmpty();
    }

    @Test
    void existsByMotIgnoreCaseEstInsensibleALaCasse() {
        assertThat(repository.existsByMotIgnoreCase("maison")).isTrue();
        assertThat(repository.existsByMotIgnoreCase("MAISON")).isTrue();
        assertThat(repository.existsByMotIgnoreCase("MaIsOn")).isTrue();
        assertThat(repository.existsByMotIgnoreCase("zzzzzz")).isFalse();
    }

    @Test
    void cleanupRefuseLesPrenomsEtMotsAnglais() {
        assertThat(repository.existsByMotIgnoreCase("MARIE")).isFalse();
        assertThat(repository.existsByMotIgnoreCase("TWIST")).isFalse();
    }

    @Test
    void findRandomJouableExclutLesFormesConjugueesEnEz() {
        // PRIEZ (jeu de test) est une conjugaison, elle ne doit jamais sortir comme reponse,
        // meme si elle reste un mot valide pour /words/validate (existsByMotIgnoreCase).
        for (int i = 0; i < 200; i++) {
            Mot tirage = repository.findRandomJouableByLongueur(5);
            assertThat(tirage.getMot()).isNotEqualTo("PRIEZ");
        }
    }

    @Test
    void findRandomJouableAutoriseLesExceptionsConnues() {
        // ASSEZ se termine en -EZ mais n'est pas un verbe conjugue : il doit rester tirable.
        assertThat(repository.existsByMotIgnoreCase("ASSEZ")).isTrue();
    }

    @Test
    void findRandomJouableExclutLePasseSimpleQuandLInfinitifExiste() {
        // BRAQUA vient de BRAQUER : le mot reste valide, mais ne doit pas devenir mot mystere.
        assertThat(repository.existsByMotIgnoreCase("BRAQUA")).isTrue();
        for (int i = 0; i < 200; i++) {
            Mot tirage = repository.findRandomJouableByLongueur(6);
            assertThat(tirage.getMot()).isNotEqualTo("BRAQUA");
        }
    }

    @Test
    void findRandomJouableExclutLesPlurielsMaisLesGardeValides() {
        // SALLES est un vrai mot : il doit etre accepte comme proposition,
        // mais le mot mystere doit preferer le singulier SALLE.
        assertThat(repository.existsByMotIgnoreCase("SALLES")).isTrue();
        for (int i = 0; i < 200; i++) {
            Mot tirage = repository.findRandomJouableByLongueur(6);
            assertThat(tirage.getMot()).isNotEqualTo("SALLES");
        }
    }

    @Test
    void countByLongueurCompteSeulementLesMotsJouables() {
        List<Object[]> lignes = repository.countByLongueur();
        assertThat(lignes).anySatisfy(row -> {
            assertThat(((Number) row[0]).intValue()).isEqualTo(5);
            assertThat(((Number) row[1]).longValue()).isEqualTo(7L);
        });
    }
}
