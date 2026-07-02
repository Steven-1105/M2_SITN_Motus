package fr.dauphine.miageif.motus.dict.repository;

import fr.dauphine.miageif.motus.dict.entity.Mot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Repository Spring Data (query methods, aucune implementation a ecrire).
public interface MotRepository extends JpaRepository<Mot, Long> {

    // SELECT * FROM mot WHERE longueur = ?
    List<Mot> findByLongueur(int longueur);

    // Existence d'un mot, insensible a la casse (utilise pour la validation).
    boolean existsByMotIgnoreCase(String mot);

    // Tirage aleatoire EFFICACE : ne charge qu'UNE ligne (indispensable avec 130k+ mots).
    @Query(value = "SELECT * FROM mot WHERE longueur = :longueur ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Mot findRandomByLongueur(@Param("longueur") int longueur);

    // Nombre de mots par longueur, sans charger les mots.
    @Query("SELECT m.longueur, COUNT(m) FROM Mot m GROUP BY m.longueur ORDER BY m.longueur")
    List<Object[]> countByLongueur();
}
