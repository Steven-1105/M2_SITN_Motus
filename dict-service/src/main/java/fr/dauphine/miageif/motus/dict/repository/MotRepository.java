package fr.dauphine.miageif.motus.dict.repository;

import fr.dauphine.miageif.motus.dict.entity.Mot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Repository Spring Data.
// - Toute proposition du joueur est validee via existsByMotIgnoreCase
//   (le dictionnaire large accepte les conjugaisons, pluriels, etc.).
// - Le mot mystere est tire uniquement parmi les mots marques jouable=true
//   dans data-jouable.sql (mots francais courants et non ambigus).
public interface MotRepository extends JpaRepository<Mot, Long> {

    // SELECT * FROM mot WHERE longueur = ?
    List<Mot> findByLongueur(int longueur);

    // Existence d'un mot, insensible a la casse. Sert a valider une proposition.
    boolean existsByMotIgnoreCase(String mot);

    // Tire un mot mystere aleatoire de la longueur demandee, uniquement parmi
    // ceux marques jouable=true.
    @Query(value =
            "SELECT * FROM mot WHERE longueur = :longueur AND jouable = true ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Mot findRandomJouableByLongueur(@Param("longueur") int longueur);

    // Nombre de mots jouables par longueur, sans charger les mots.
    // Utilise par /words/lengths pour peupler le selecteur de niveau du front.
    @Query("SELECT m.longueur, COUNT(m) FROM Mot m "
            + "WHERE m.longueur BETWEEN 5 AND 9 AND m.jouable = true "
            + "GROUP BY m.longueur ORDER BY m.longueur")
    List<Object[]> countByLongueur();
}
