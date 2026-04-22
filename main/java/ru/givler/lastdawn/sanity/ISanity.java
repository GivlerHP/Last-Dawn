package ru.givler.lastdawn.sanity;

public interface ISanity {
    int getSanity();
    void setSanity(int value);
    int getMaxSanity();
    boolean hasSpawnedWarden();
    void setSpawnedWarden(boolean value);

    SanityStage getPreviousStage();
    void setPreviousStage(SanityStage stage);

    default void addSanity(int delta) { setSanity(getSanity() + delta); }
    default void reduceSanity(int delta) { setSanity(getSanity() - delta); }

    SanityStage getStage();
}