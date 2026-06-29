package fr.dauphine.miageif.motus.dict_service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository Spring Data : aucune implementation a ecrire (cf. cours)
public interface MotRepository extends JpaRepository<Mot, Long> {

	// SELECT * FROM mot WHERE longueur = ?
	List<Mot> findByLongueur(int longueur);

	// Existence d'un mot, insensible a la casse
	boolean existsByMotIgnoreCase(String mot);
}
