package fr.dauphine.miageif.motus.score.repository;

import fr.dauphine.miageif.motus.score.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository Spring Data (query methods, aucune implementation a ecrire).
public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    // Pour l'idempotence : un resultat par partie.
    Optional<GameResult> findByGameId(Long gameId);

    List<GameResult> findByPlayerId(Long playerId);
}
