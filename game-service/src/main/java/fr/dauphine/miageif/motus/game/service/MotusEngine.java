package fr.dauphine.miageif.motus.game.service;

import fr.dauphine.miageif.motus.game.dto.LetterResult;
import fr.dauphine.miageif.motus.game.entity.LetterStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MotusEngine {

    public List<LetterResult> compare(String guess, String target) {
        int length = target.length();
        LetterStatus[] statuses = new LetterStatus[length];
        Map<Character, Integer> remaining = new HashMap<>();

        for (int i = 0; i < length; i++) {
            char targetChar = target.charAt(i);
            if (guess.charAt(i) == targetChar) {
                statuses[i] = LetterStatus.BIEN_PLACE;
            } else {
                remaining.merge(targetChar, 1, Integer::sum);
            }
        }

        for (int i = 0; i < length; i++) {
            if (statuses[i] != null) {
                continue;
            }
            char guessChar = guess.charAt(i);
            int available = remaining.getOrDefault(guessChar, 0);
            if (available > 0) {
                statuses[i] = LetterStatus.MAL_PLACE;
                remaining.put(guessChar, available - 1);
            } else {
                statuses[i] = LetterStatus.ABSENT;
            }
        }

        List<LetterResult> results = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            results.add(new LetterResult(String.valueOf(guess.charAt(i)), statuses[i]));
        }
        return results;
    }

    public boolean isWin(List<LetterResult> results) {
        return results.stream().allMatch(r -> r.getStatut() == LetterStatus.BIEN_PLACE);
    }
}
