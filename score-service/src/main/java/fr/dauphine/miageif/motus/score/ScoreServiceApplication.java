package fr.dauphine.miageif.motus.score;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Point d'entree du microservice score-service
@SpringBootApplication
public class ScoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreServiceApplication.class, args);
    }
}
