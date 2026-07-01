package fr.dauphine.miageif.motus.dict.controller;

import fr.dauphine.miageif.motus.dict.dto.LevelInfo;
import fr.dauphine.miageif.motus.dict.dto.ValidResponse;
import fr.dauphine.miageif.motus.dict.dto.WordRequest;
import fr.dauphine.miageif.motus.dict.dto.WordResponse;
import fr.dauphine.miageif.motus.dict.entity.Mot;
import fr.dauphine.miageif.motus.dict.exception.WordNotFoundException;
import fr.dauphine.miageif.motus.dict.repository.MotRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// API REST du dictionnaire - niveau 2 du modele de Richardson (cf. cours Partie IV)
@RestController
@RequestMapping("/words")
public class MotController {

    private final MotRepository repository;

    // Injection par constructeur (preferee a @Autowired sur champ)
    public MotController(MotRepository repository) {
        this.repository = repository;
    }

    // GET /words/random?length=6  ->  { "word": "MAISON" }
    // Appele par game-service pour demarrer une partie.
    @GetMapping("/random")
    public WordResponse motAleatoire(@RequestParam(defaultValue = "6") int length) {
        List<Mot> mots = repository.findByLongueur(length);
        if (mots.isEmpty()) {
            throw new WordNotFoundException("Aucun mot de longueur " + length);
        }
        Mot choisi = mots.get(ThreadLocalRandom.current().nextInt(mots.size()));
        return new WordResponse(choisi.getMot());
    }

    // POST /words/validate  body { "word": "MOUTON" }  ->  { "valid": true }
    // Contrat impose par game-service (Steven).
    @PostMapping("/validate")
    public ValidResponse validerMot(@RequestBody WordRequest requete) {
        boolean valid = requete.getWord() != null
                && repository.existsByMotIgnoreCase(requete.getWord());
        return new ValidResponse(valid);
    }

    // GET /words/exists?word=MOUTON  ->  { "valid": true }
    // Variante plus "RESTful" (une verification est une LECTURE idempotente).
    @GetMapping("/exists")
    public ValidResponse motExiste(@RequestParam String word) {
        return new ValidResponse(repository.existsByMotIgnoreCase(word));
    }

    // GET /words/lengths  ->  [ { "length": 5, "count": 12 }, { "length": 6, "count": 17 }, ... ]
    // Longueurs disponibles (= niveaux) avec le nombre de mots, pour le selecteur de difficulte.
    @GetMapping("/lengths")
    public List<LevelInfo> longueursDisponibles() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Mot::getLongueur, TreeMap::new, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new LevelInfo(e.getKey(), e.getValue()))
                .toList();
    }
}
