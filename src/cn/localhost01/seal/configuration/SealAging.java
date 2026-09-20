package cn.localhost01.seal.configuration;

/**
 * 印章老化效果配置。默认关闭，因此不会改变原 SealUtil 的输出。
 */
public class SealAging {
    private boolean enabled = false;
    private int intensity = 35;
    private int grainSize = 2;
    private int damageSize = 4;
    private int scratchStrength = 10;
    private int offsetX = 0;
    private int offsetY = 0;
    private long seed = 43821L;

    public boolean isEnabled() { return enabled; }
    public int getIntensity() { return intensity; }
    public int getGrainSize() { return grainSize; }
    public int getDamageSize() { return damageSize; }
    public int getScratchStrength() { return scratchStrength; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public long getSeed() { return seed; }

    public SealAging setEnabled(boolean enabled) { this.enabled = enabled; return this; }
    public SealAging setIntensity(int intensity) { this.intensity = clamp(intensity, 0, 100); return this; }
    public SealAging setGrainSize(int grainSize) { this.grainSize = clamp(grainSize, 1, 12); return this; }
    public SealAging setDamageSize(int damageSize) { this.damageSize = clamp(damageSize, 1, 24); return this; }
    public SealAging setScratchStrength(int scratchStrength) { this.scratchStrength = clamp(scratchStrength, 0, 100); return this; }
    public SealAging setOffsetX(int offsetX) { this.offsetX = offsetX; return this; }
    public SealAging setOffsetY(int offsetY) { this.offsetY = offsetY; return this; }
    public SealAging setSeed(long seed) { this.seed = seed; return this; }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
