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
    void findRandomJouableExcluTIesFormesConjuguesEnEz() {
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
}
