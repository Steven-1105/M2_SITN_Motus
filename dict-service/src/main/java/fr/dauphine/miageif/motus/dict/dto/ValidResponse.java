package fr.dauphine.miageif.motus.dict.dto;

// Reponse JSON de POST /words/validate et GET /words/exists : { "valid": true }
public class ValidResponse {

    private boolean valid;

    public ValidResponse() {
    }

    public ValidResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
