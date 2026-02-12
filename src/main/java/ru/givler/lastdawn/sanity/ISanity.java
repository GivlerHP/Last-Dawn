package ru.givler.lastdawn.sanity;

public interface ISanity {
    int getSanity();
    void setSanity(int value);
    boolean hasSpawnedWarden();
    void setSpawnedWarden(boolean value);

    default void addSanity(int delta) { setSanity(getSanity() + delta); }
    default void reduceSanity(int delta) { setSanity(getSanity() - delta); }

    SanityStage getStage();
}