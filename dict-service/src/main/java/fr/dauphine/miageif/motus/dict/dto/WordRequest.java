package fr.dauphine.miageif.motus.dict.dto;

// Corps JSON attendu par POST /words/validate : { "word": "MOUTON" }
public class WordRequest {

    private String word;

    public WordRequest() {
    }

    public WordRequest(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
