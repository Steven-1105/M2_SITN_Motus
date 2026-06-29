package fr.dauphine.miageif.motus.dict_service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
	@GetMapping("/random")
	public ResponseEntity<?> motAleatoire(@RequestParam(defaultValue = "6") int length) {
		List<Mot> mots = repository.findByLongueur(length);
		if (mots.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("Aucun mot de longueur " + length, 404));
		}
		Mot choisi = mots.get(ThreadLocalRandom.current().nextInt(mots.size()));
		return ResponseEntity.ok(new WordResponse(choisi.getMot()));
	}

	// GET /words/exists?word=MOUTON  ->  { "valid": true }
	// Version conforme REST : une verification est une LECTURE (GET idempotent).
	@GetMapping("/exists")
	public ResponseEntity<ValidResponse> motExiste(@RequestParam String word) {
		boolean valid = repository.existsByMotIgnoreCase(word);
		return ResponseEntity.ok(new ValidResponse(valid));
	}

	// POST /words/validate  ->  { "valid": true }
	// Conserve pour respecter le contrat initial avec game-service (Steven).
	// A retirer quand game-service appellera GET /words/exists.
	@PostMapping("/validate")
	public ResponseEntity<ValidResponse> validerMot(@RequestBody WordRequest requete) {
		boolean valid = requete.word() != null
				&& repository.existsByMotIgnoreCase(requete.word());
		return ResponseEntity.ok(new ValidResponse(valid));
	}
}
