package fr.dauphine.miageif.motus.dict_service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entite JPA representant un mot du dictionnaire (cf. cours - TauxChange)
@Entity
@Table(name = "mot")
public class Mot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mot", nullable = false, unique = true, length = 50)
	private String mot;

	@Column(name = "longueur", nullable = false)
	private int longueur;

	public Mot() {
	}

	public Mot(String mot) {
		setMot(mot);
	}

	public Long getId() {
		return id;
	}

	public String getMot() {
		return mot;
	}

	public void setMot(String mot) {
		this.mot = mot;
		this.longueur = (mot != null) ? mot.length() : 0;
	}

	public int getLongueur() {
		return longueur;
	}
}
