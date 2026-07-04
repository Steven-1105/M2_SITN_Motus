package fr.dauphine.miageif.motus.dict.repository;

import fr.dauphine.miageif.motus.dict.entity.Mot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

// Repository Spring Data (query methods, aucune implementation a ecrire).
public interface MotRepository extends JpaRepository<Mot, Long> {

    Set<String> MOTS_JAMAIS_JOUABLES = Set.of(
            "POINS", "POIGNIS", "POIGNIT", "OIGNIS", "OIGNIT",
            "ABIMONS", "ABIMEZ", "ABIMAT",
            "ABATTIS", "ABATTIT",
            "ACHOPPAS", "ACHOPPAT",
            "AILEZ", "AILAIT",
            "OYEZ", "OYIONS", "OYAIT"
    );

    Set<String> MOTS_TOUJOURS_JOUABLES = Set.of(
            "ASSEZ", "CHEZ", "SUISSE", "CAISSE", "LAISSE", "BAISSE",
            "BROSSE", "POISSE", "MOUSSE", "CROSSE", "FESSE", "MASSE",
            "CASSE", "TASSE", "PASSE", "LASSE", "RUSSE", "BOSSE",
            "OPERA", "SOURIS", "TAPIS"
    );

    String[] TERMINAISONS_CONJUGUEES = {
            "ISSAIENT", "ISSIONS", "ASSIONS", "USSIONS",
            "ERAIENT", "IRAIENT", "ERIONS", "IRIONS",
            "ERAIS", "ERAIT", "ERONS", "ERONT",
            "IRAIS", "IRAIT", "IRONS", "IRONT",
            "ISSAIS", "ISSAIT", "ISSIEZ", "ISSONS", "ISSANT",
            "ASSENT", "ISSENT", "USSENT", "ASSES", "ISSES", "USSES",
            "ASSIEZ", "USSIEZ", "ISSEZ",
            "ERENT", "IRENT", "ASSE", "ISSE", "USSE",
            "AMES", "ATES", "IMES", "ITES",
            "AIENT", "IONS", "IEZ", "AIS", "AIT", "ONS", "ONT",
            "EZ", "AI", "AS", "AT", "IS", "IT", "A"
    };

    String[] RECONSTRUCTIONS_INFINITIF = {"", "ER", "IR", "RE", "R", "E"};

    // SELECT * FROM mot WHERE longueur = ?
    List<Mot> findByLongueur(int longueur);

    // Existence d'un mot, insensible a la casse (utilise pour la validation).
    boolean existsByMotIgnoreCase(String mot);

    // Tire un petit lot aleatoire, puis le filtre en Java pour eviter les mots trop
    // difficiles a deviner (conjugaisons, pluriels simples).
    // Ces mots restent valides comme propositions du joueur via existsByMotIgnoreCase.
    @Query(value =
            "SELECT * FROM mot WHERE longueur = :longueur AND jouable = true ORDER BY RAND() LIMIT 200",
            nativeQuery = true)
    List<Mot> findRandomCandidatesByLongueur(@Param("longueur") int longueur);

    default Mot findRandomJouableByLongueur(int longueur) {
        return findRandomCandidatesByLongueur(longueur).stream()
                .filter(mot -> estMotJouable(mot.getMot()))
                .findFirst()
                .orElse(null);
    }

    // Nombre de mots par longueur, sans charger les mots.
    // On expose uniquement les niveaux JOUABLES (5 a 9 lettres) : les mots de 4 lettres
    // sont trop faciles a deviner et polluent le classement.
    @Query("SELECT m.longueur, COUNT(m) FROM Mot m "
            + "WHERE m.longueur BETWEEN 5 AND 9 AND m.jouable = true "
            + "GROUP BY m.longueur ORDER BY m.longueur")
    List<Object[]> countByLongueur();

    private boolean estMotJouable(String mot) {
        if (mot == null) {
            return true;
        }
        if (MOTS_JAMAIS_JOUABLES.contains(mot)) {
            return false;
        }
        if (MOTS_TOUJOURS_JOUABLES.contains(mot)) {
            return true;
        }
        return !ressembleAUneConjugaison(mot)
                && !ressembleAUnPlurielSimple(mot);
    }

    private boolean ressembleAUneConjugaison(String mot) {
        for (String terminaison : TERMINAISONS_CONJUGUEES) {
            if (mot.endsWith(terminaison) && correspondAUnInfinitif(mot, terminaison)) {
                return true;
            }
        }
        return false;
    }

    private boolean correspondAUnInfinitif(String mot, String terminaison) {
        String radical = mot.substring(0, mot.length() - terminaison.length());
        for (String reconstruction : RECONSTRUCTIONS_INFINITIF) {
            String infinitif = radical + reconstruction;
            if (!infinitif.equals(mot) && estInfinitif(infinitif) && existsByMotIgnoreCase(infinitif)) {
                return true;
            }
        }
        return false;
    }

    private boolean ressembleAUnPlurielSimple(String mot) {
        if (mot.length() < 6 || !mot.endsWith("S")) {
            return false;
        }
        String singulier = mot.substring(0, mot.length() - 1);
        return existsByMotIgnoreCase(singulier);
    }

    private static boolean estInfinitif(String mot) {
        return mot.length() >= 5
                && (mot.endsWith("ER") || mot.endsWith("IR") || mot.endsWith("RE") || mot.endsWith("OIR"));
    }
}
