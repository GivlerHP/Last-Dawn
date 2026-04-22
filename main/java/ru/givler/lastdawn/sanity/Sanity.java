package ru.givler.lastdawn.sanity;

public class Sanity implements ISanity {
    private int sanity = 0; // 0-100
    private boolean spawnedWarden = false;
    private SanityStage previousStage = SanityStage.SANE;

    @Override
    public boolean hasSpawnedWarden() { return spawnedWarden; }
    @Override
    public void setSpawnedWarden(boolean value) { spawnedWarden = value; }

    @Override
    public int getSanity() { return sanity; }

    @Override
    public void setSanity(int value) {
        int clamped = Math.max(0, Math.min(100, value));
        sanity = clamped;
    }

    @Override
    public SanityStage getPreviousStage() { return previousStage; }

    @Override
    public void setPreviousStage(SanityStage stage) { previousStage = stage; }

    public int getMaxSanity() { return 100; }

    @Override
    public SanityStage getStage() {
        if (sanity <= 25) return SanityStage.SANE;
        if (sanity <= 50) return SanityStage.ANXIOUS;
        if (sanity <= 75) return SanityStage.PSYCHOSIS;
        return SanityStage.INSANITY;
    }
}