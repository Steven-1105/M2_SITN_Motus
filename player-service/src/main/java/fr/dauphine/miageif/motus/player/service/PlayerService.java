package fr.dauphine.miageif.motus.player.service;

import fr.dauphine.miageif.motus.player.dto.LoginRequest;
import fr.dauphine.miageif.motus.player.dto.PlayerCreateRequest;
import fr.dauphine.miageif.motus.player.dto.PlayerResponse;
import fr.dauphine.miageif.motus.player.entity.Player;
import fr.dauphine.miageif.motus.player.entity.Role;
import fr.dauphine.miageif.motus.player.exception.DuplicateResourceException;
import fr.dauphine.miageif.motus.player.exception.InvalidCredentialsException;
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

    // Connexion : accepte pseudo OU email dans 'identifiant', verifie le mot de passe
    // via BCrypt (le meme encoder utilise a l'inscription).
    public PlayerResponse login(LoginRequest request) {
        Player player = playerRepository.findByUsername(request.getIdentifiant())
                .or(() -> playerRepository.findByEmail(request.getIdentifiant()))
                .orElseThrow(() -> new InvalidCredentialsException("Identifiant ou mot de passe incorrect"));
        if (!passwordEncoder.matches(request.getPassword(), player.getPassword())) {
            throw new InvalidCredentialsException("Identifiant ou mot de passe incorrect");
        }
        return PlayerResponse.fromEntity(player);
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
