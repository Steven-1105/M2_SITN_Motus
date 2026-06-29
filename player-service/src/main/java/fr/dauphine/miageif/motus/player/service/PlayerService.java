package fr.dauphine.miageif.motus.player.service;

import fr.dauphine.miageif.motus.player.dto.PlayerCreateRequest;
import fr.dauphine.miageif.motus.player.dto.PlayerResponse;
import fr.dauphine.miageif.motus.player.entity.Player;
import fr.dauphine.miageif.motus.player.entity.Role;
import fr.dauphine.miageif.motus.player.exception.DuplicateResourceException;
import fr.dauphine.miageif.motus.player.exception.ResourceNotFoundException;
import fr.dauphine.miageif.motus.player.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PlayerResponse register(PlayerCreateRequest request) {
        if (playerRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Player player = new Player(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.PLAYER
        );

        Player saved = playerRepository.save(player);
        return PlayerResponse.fromEntity(saved);
    }

    public PlayerResponse getById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        return PlayerResponse.fromEntity(player);
    }

    public List<PlayerResponse> getAll() {
        return playerRepository.findAll().stream()
                .map(PlayerResponse::fromEntity)
                .toList();
    }
}
