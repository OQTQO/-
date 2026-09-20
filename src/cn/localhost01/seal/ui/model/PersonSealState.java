package cn.localhost01.seal.ui.model;

import cn.localhost01.seal.configuration.SealAging;
import cn.localhost01.seal.configuration.SealFont;

public final class PersonSealState {
    public String text = "诸葛孔明";
    public String addText = "印";
    public String family = "宋体";
    public int fontSize = 120;
    public double fontSpace = 10.0;
    public boolean bold = true;
    public int offsetX = 0;
    public int offsetY = 0;
    public int imageSize = 300;
    public int lineSize = 16;

    public final SealAging aging = new SealAging()
            .setEnabled(false)
            .setIntensity(35)
            .setGrainSize(2)
            .setDamageSize(4)
            .setScratchStrength(10)
            .setSeed(43821L);

    public SealFont toFont() {
        return new SealFont()
                .setFontText(text)
                .setFontFamily(family)
                .setFontSize(fontSize)
                .setFontSpace(fontSpace)
                .setBold(bold)
                .setOffsetX(offsetX)
                .setOffsetY(offsetY);
    }

    public SealAging copyAging() {
        return new SealAging()
                .setEnabled(aging.isEnabled())
                .setIntensity(aging.getIntensity())
                .setGrainSize(aging.getGrainSize())
                .setDamageSize(aging.getDamageSize())
                .setScratchStrength(aging.getScratchStrength())
                .setOffsetX(aging.getOffsetX())
                .setOffsetY(aging.getOffsetY())
                .setSeed(aging.getSeed());
    }

    public void reset() {
        text = "诸葛孔明";
        addText = "印";
        family = "宋体";
        fontSize = 120;
        fontSpace = 10.0;
        bold = true;
        offsetX = 0;
        offsetY = 0;
        imageSize = 300;
        lineSize = 16;
        aging.setEnabled(false)
                .setIntensity(35)
                .setGrainSize(2)
                .setDamageSize(4)
                .setScratchStrength(10)
                .setOffsetX(0)
                .setOffsetY(0)
                .setSeed(43821L);
    }
}
