package fr.dauphine.miageif.motus.dict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Point d'entree du microservice dict-service (cf. cours : MsaApplication)
@SpringBootApplication
public class DictServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DictServiceApplication.class, args);
    }
}
