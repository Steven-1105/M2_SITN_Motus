package fr.dauphine.miageif.motus.score.controller;

import fr.dauphine.miageif.motus.score.dto.GameResultRequest;
import fr.dauphine.miageif.motus.score.dto.GameResultResponse;
import fr.dauphine.miageif.motus.score.dto.PlayerStats;
import fr.dauphine.miageif.motus.score.dto.RankingEntry;
import fr.dauphine.miageif.motus.score.service.ScoreService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    // POST /scores/results : appele par game-service a la fin d'une partie (modele push).
    @PostMapping("/results")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResultResponse enregistrer(@RequestBody GameResultRequest req) {
        return service.record(req);
    }

    // GET /scores/ranking : classement global des joueurs.
    @GetMapping("/ranking")
    public List<RankingEntry> ranking() {
        return service.ranking();
    }

    // GET /scores/players/{id} : statistiques d'un joueur (victoires, defaites, moyenne).
    @GetMapping("/players/{id}")
    public PlayerStats statsJoueur(@PathVariable Long id) {
        return service.playerStats(id);
    }

    // GET /scores/games : liste des parties pour l'admin (filtres : playerId, from, to au format AAAA-MM-JJ).
    @GetMapping("/games")
    public List<GameResultResponse> parties(
            @RequestParam(required = false) Long playerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.games(playerId, from, to);
    }
}
