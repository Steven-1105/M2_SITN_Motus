package fr.dauphine.miageif.motus.dict.dto;

// Un "niveau" = une longueur de mot disponible et le nombre de mots correspondants.
// Sert au front pour proposer un choix de difficulte (plus le mot est long, plus c'est dur).
public record LevelInfo(int length, long count) {
}
