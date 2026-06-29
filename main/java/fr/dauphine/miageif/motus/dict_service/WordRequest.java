package fr.dauphine.miageif.motus.dict_service;

// Corps JSON attendu pour POST /words/validate : { "word": "MOUTON" }
public record WordRequest(String word) {
}
