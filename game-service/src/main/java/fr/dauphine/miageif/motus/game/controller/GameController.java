package fr.dauphine.miageif.motus.game.controller;

import fr.dauphine.miageif.motus.game.dto.GameCreateRequest;
import fr.dauphine.miageif.motus.game.dto.GameResponse;
import fr.dauphine.miageif.motus.game.dto.GuessRequest;
import fr.dauphine.miageif.motus.game.dto.LetterResult;
import fr.dauphine.miageif.motus.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameCreateRequest request) {
        GameResponse response = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/guess")
    public ResponseEntity<List<LetterResult>> guess(@PathVariable Long id, @Valid @RequestBody GuessRequest request) {
        return ResponseEntity.ok(gameService.guess(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getGames(@RequestParam(required = false) Long playerId) {
        return ResponseEntity.ok(gameService.getGamesByPlayer(playerId));
    }
}
