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
    // Exclut les formes CONJUGUEES (trop dures a deviner comme reponse), mais elles restent
    // valides comme PROPOSITION du joueur (cf. existsByMotIgnoreCase, utilise par /words/validate).
    // Suffixes exclus :
    //   - EZ/IEZ  (vous ...ez)             : ex. PRIEZ, PLIEZ
    //   - ERAI, ERAS, ERONS, ERONT         : futur simple
    //   - ERAIS, ERAIT, ERIONS, ERAIENT    : conditionnel present
    //   - AIS, AIT, AIENT, IONS            : imparfait (attention : IONS peut etre un nom)
    //   - ASSE, ASSES, ASSENT, ASSIONS     : subjonctif imparfait
    //   - ISSE, ISSES, ISSENT, ISSIONS     : subjonctif imparfait -IR
    //   - USSE, USSES, USSENT, USSIONS     : subjonctif imparfait -OIR
    // Exceptions (noms/adjectifs frequents mal captures par la regex) : ASSEZ, CHEZ, MAIS,
    //   FRAIS, VRAIS, LAIT, FAIT, TRAIT, LIONS, PAONS, PIONS, CANONS...
    @Query(value =
        "SELECT * FROM mot WHERE longueur = :longueur "
      + "AND (mot NOT REGEXP '(EZ|ERAI|ERAS|ERONS|ERONT|ERAIS|ERAIT|ERIONS|ERAIENT|AIENT|ASSE|ASSES|ASSENT|ASSIONS|ISSE|ISSES|ISSENT|ISSIONS|USSE|USSES|USSENT|USSIONS)$' "
      + "     OR mot IN ('ASSEZ','CHEZ','SUISSE','CAISSE','LAISSE','BAISSE','BROSSE','POISSE','MOUSSE','CROSSE','FESSE','MASSE','CASSE','TASSE','PASSE','LASSE','RUSSE','BOSSE')) "
      + "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Mot findRandomJouableByLongueur(@Param("longueur") int longueur);

    // Nombre de mots par longueur, sans charger les mots.
    // On expose uniquement les niveaux JOUABLES (5 a 9 lettres) : les mots de 4 lettres
    // sont trop faciles a deviner et polluent le classement.
    @Query("SELECT m.longueur, COUNT(m) FROM Mot m "
            + "WHERE m.longueur BETWEEN 5 AND 9 "
            + "GROUP BY m.longueur ORDER BY m.longueur")
    List<Object[]> countByLongueur();
}
