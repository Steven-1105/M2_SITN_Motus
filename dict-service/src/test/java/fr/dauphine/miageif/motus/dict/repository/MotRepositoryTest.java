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
    void findRandomJouableNeSortJamaisDeMotJouableFalse() {
        // PRIEZ, BRAQUA, SALLES sont en base avec jouable=false : ils doivent
        // rester valides comme propositions (existsByMotIgnoreCase = true) mais
        // ne jamais etre tires comme mot mystere.
        assertThat(repository.existsByMotIgnoreCase("PRIEZ")).isTrue();
        assertThat(repository.existsByMotIgnoreCase("BRAQUA")).isTrue();
        assertThat(repository.existsByMotIgnoreCase("SALLES")).isTrue();
        for (int i = 0; i < 100; i++) {
            String tirage5 = repository.findRandomJouableByLongueur(5).getMot();
            String tirage6 = repository.findRandomJouableByLongueur(6).getMot();
            assertThat(tirage5).isNotEqualTo("PRIEZ");
            assertThat(tirage6).isNotEqualTo("BRAQUA");
            assertThat(tirage6).isNotEqualTo("SALLES");
        }
    }

    @Test
    void findRandomJouableRetourneUniquementDesMotsMarquesJouable() {
        // Verifie que toutes les longueurs 5-9 renvoient bien un mot jouable=true.
        for (int longueur = 5; longueur <= 9; longueur++) {
            Mot tirage = repository.findRandomJouableByLongueur(longueur);
            assertThat(tirage).isNotNull();
            assertThat(tirage.isJouable()).isTrue();
            assertThat(tirage.getLongueur()).isEqualTo(longueur);
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
