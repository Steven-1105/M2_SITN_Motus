package fr.dauphine.miageif.motus.score.exception;

// Format d'erreur commun convenu dans le binome : { "error": "...", "status": 404 }
public class ErrorResponse {

    private String error;
    private int status;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
