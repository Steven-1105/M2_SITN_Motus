package com.motus.game.repository;

import com.motus.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByPlayerId(Long playerId);
}
