package fr.dauphine.miageif.motus.game.service;

import fr.dauphine.miageif.motus.game.dto.*;
import fr.dauphine.miageif.motus.game.entity.Game;
import fr.dauphine.miageif.motus.game.entity.GameAttempt;
import fr.dauphine.miageif.motus.game.entity.GameStatus;
import fr.dauphine.miageif.motus.game.exception.InvalidGameStateException;
import fr.dauphine.miageif.motus.game.exception.InvalidWordException;
import fr.dauphine.miageif.motus.game.exception.ResourceNotFoundException;
import fr.dauphine.miageif.motus.game.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final DictServiceClient dictServiceClient;
    private final MotusEngine motusEngine;

    public GameService(GameRepository gameRepository, DictServiceClient dictServiceClient, MotusEngine motusEngine) {
        this.gameRepository = gameRepository;
        this.dictServiceClient = dictServiceClient;
        this.motusEngine = motusEngine;
    }

    public GameResponse createGame(GameCreateRequest request) {
        String word = dictServiceClient.getRandomWord(request.getWordLength());

        Game game = new Game(request.getPlayerId(), word, request.getWordLength(), request.getMaxAttempts());
        Game saved = gameRepository.save(game);

        return GameResponse.fromEntity(saved, List.of());
    }

    public List<LetterResult> guess(Long gameId, GuessRequest request) {
        Game game = findGameOrThrow(gameId);

        if (game.getStatut() != GameStatus.EN_COURS) {
            throw new InvalidGameStateException("Game " + gameId + " is already finished");
        }

        String word = request.getWord().trim().toUpperCase();
        if (word.length() != game.getWordLength()) {
            throw new InvalidWordException("Word must be " + game.getWordLength() + " letters long");
        }
        if (!dictServiceClient.isWordValid(word)) {
            throw new InvalidWordException("Word is not a valid French word: " + word);
        }

        List<LetterResult> results = motusEngine.compare(word, game.getMotMystere());

        int attemptNumber = game.getAttemptsUsed() + 1;
        String pattern = results.stream().map(r -> r.getStatut().name()).collect(Collectors.joining(","));
        game.getAttempts().add(new GameAttempt(game, attemptNumber, word, pattern));
        game.setAttemptsUsed(attemptNumber);

        if (motusEngine.isWin(results)) {
            game.setStatut(GameStatus.GAGNE);
        } else if (attemptNumber >= game.getMaxAttempts()) {
            game.setStatut(GameStatus.PERDU);
        }

        gameRepository.save(game);
        return results;
    }

    public GameResponse getGame(Long gameId) {
        Game game = findGameOrThrow(gameId);
        return GameResponse.fromEntity(game, toAttemptResponses(game));
    }

    public List<GameResponse> getGamesByPlayer(Long playerId) {
        List<Game> games = playerId != null
                ? gameRepository.findByPlayerId(playerId)
                : gameRepository.findAll();

        return games.stream()
                .map(game -> GameResponse.fromEntity(game, toAttemptResponses(game)))
                .toList();
    }

    private List<AttemptResponse> toAttemptResponses(Game game) {
        return game.getAttempts().stream()
                .map(attempt -> new AttemptResponse(
                        attempt.getAttemptNumber(),
                        attempt.getWord(),
                        motusEngine.compare(attempt.getWord(), game.getMotMystere())
                ))
                .toList();
    }

    private Game findGameOrThrow(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }
}
