package com.motus.game.service;

import com.motus.game.dto.LetterResult;
import com.motus.game.entity.LetterStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MotusEngineTest {

    private final MotusEngine engine = new MotusEngine();

    @Test
    void exactMatchIsAWin() {
        List<LetterResult> results = engine.compare("MAISON", "MAISON");

        assertThat(results).allMatch(r -> r.getStatut() == LetterStatus.BIEN_PLACE);
        assertThat(engine.isWin(results)).isTrue();
    }

    @Test
    void detectsMisplacedAndAbsentLetters() {
        List<LetterResult> results = engine.compare("MOUTON", "MAISON");

        assertThat(results.get(0).getStatut()).isEqualTo(LetterStatus.BIEN_PLACE); // M
        assertThat(results.get(1).getStatut()).isEqualTo(LetterStatus.ABSENT);     // O
        assertThat(results.get(5).getStatut()).isEqualTo(LetterStatus.BIEN_PLACE); // N
        assertThat(engine.isWin(results)).isFalse();
    }

    @Test
    void handlesDuplicateLettersCorrectly() {
        // target "POMME" has two M's; guess "MOMIE" should mark only one M as misplaced
        List<LetterResult> results = engine.compare("MOMIE", "POMME");

        assertThat(results.get(0).getStatut()).isEqualTo(LetterStatus.MAL_PLACE);  // M
        assertThat(results.get(1).getStatut()).isEqualTo(LetterStatus.BIEN_PLACE); // O
        assertThat(results.get(2).getStatut()).isEqualTo(LetterStatus.BIEN_PLACE); // M
        assertThat(results.get(3).getStatut()).isEqualTo(LetterStatus.ABSENT);     // I
        assertThat(results.get(4).getStatut()).isEqualTo(LetterStatus.BIEN_PLACE); // E
    }
}
