package fr.dauphine.miageif.motus.player.controller;

import fr.dauphine.miageif.motus.player.dto.PlayerCreateRequest;
import fr.dauphine.miageif.motus.player.dto.PlayerResponse;
import fr.dauphine.miageif.motus.player.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody PlayerCreateRequest request) {
        PlayerResponse response = playerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAll() {
        return ResponseEntity.ok(playerService.getAll());
    }
}
