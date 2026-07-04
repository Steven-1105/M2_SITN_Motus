package fr.dauphine.miageif.motus.player.dto;

import jakarta.validation.constraints.NotBlank;

// Requete de connexion : accepte le pseudo OU l'email dans "identifiant".
public class LoginRequest {

    @NotBlank(message = "identifiant is required")
    private String identifiant;

    @NotBlank(message = "password is required")
    private String password;

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
