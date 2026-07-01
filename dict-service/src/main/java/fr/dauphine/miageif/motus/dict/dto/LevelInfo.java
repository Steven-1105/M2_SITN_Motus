package fr.dauphine.miageif.motus.dict.dto;

// Un "niveau" = une longueur de mot disponible et le nombre de mots correspondants.
// Sert au front pour proposer un choix de difficulte.
public class LevelInfo {

    private int length;
    private long count;

    public LevelInfo() {
    }

    public LevelInfo(int length, long count) {
        this.length = length;
        this.count = count;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
