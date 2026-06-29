package fr.dauphine.miageif.motus.dict.dto;

// Corps JSON attendu par POST /words/validate : { "word": "MOUTON" }
public record WordRequest(String word) {
}
