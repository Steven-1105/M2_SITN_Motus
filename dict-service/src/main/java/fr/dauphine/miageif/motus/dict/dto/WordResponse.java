package fr.dauphine.miageif.motus.dict.dto;

// Reponse JSON de GET /words/random : { "word": "MAISON" }
public class WordResponse {

    private String word;

    public WordResponse() {
    }

    public WordResponse(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
