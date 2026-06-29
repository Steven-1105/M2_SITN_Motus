package fr.dauphine.miageif.motus.dict.dto;

// Reponse JSON de POST /words/validate et GET /words/exists : { "valid": true }
public record ValidResponse(boolean valid) {
}
