package cn.localhost01.seal.test;

import cn.localhost01.seal.SealUtil;
import cn.localhost01.seal.configuration.SealAging;
import cn.localhost01.seal.configuration.SealCircle;
import cn.localhost01.seal.configuration.SealConfiguration;
import cn.localhost01.seal.configuration.SealFont;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class SealCoreSmokeTest {
    private SealCoreSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        verifyOffsetsChangeOutput();
        verifyAgingIsDeterministic();
        verifyPrivateSealPipeline();
        System.out.println("SealStudio V2 core smoke tests passed.");
    }

    private static void verifyOffsetsChangeOutput() throws Exception {
        BufferedImage base = SealUtil.buildSeal(config(0, 0, false, 42L));
        BufferedImage moved = SealUtil.buildSeal(config(12, -7, false, 42L));
        require(!samePixels(base, moved), "XY offset should change public seal pixels");
    }

    private static void verifyAgingIsDeterministic() throws Exception {
        BufferedImage clean = SealUtil.buildSeal(config(0, 0, false, 918273L));
        BufferedImage agedA = SealUtil.buildSeal(config(0, 0, true, 918273L));
        BufferedImage agedB = SealUtil.buildSeal(config(0, 0, true, 918273L));
        BufferedImage agedOther = SealUtil.buildSeal(config(0, 0, true, 918274L));

        require(!samePixels(clean, agedA), "aging should change rendered pixels");
        require(samePixels(agedA, agedB), "same aging seed must be deterministic");
        require(!samePixels(agedA, agedOther), "different aging seed should change texture");
        require(transparentBackgroundRemainsTransparent(clean, agedA),
                "aging must not paint into transparent background");
    }

    private static void verifyPrivateSealPipeline() throws Exception {
        SealFont baseFont = new SealFont()
                .setFontText("诸葛孔明")
                .setFontFamily("宋体")
                .setFontSize(120)
                .setFontSpace(10.0)
                .setBold(true)
                .setOffsetX(0)
                .setOffsetY(0);

        SealFont movedFont = new SealFont()
                .setFontText("诸葛孔明")
                .setFontFamily("宋体")
                .setFontSize(120)
                .setFontSpace(10.0)
                .setBold(true)
                .setOffsetX(8)
                .setOffsetY(5);

        BufferedImage base = SealUtil.buildPersonSeal(300, 16, baseFont, null);
        BufferedImage moved = SealUtil.buildPersonSeal(300, 16, movedFont, null);
        require(!samePixels(base, moved), "private seal XY offset should change text pixels");

        SealAging aging = new SealAging()
                .setEnabled(true)
                .setIntensity(35)
                .setGrainSize(2)
                .setDamageSize(4)
                .setScratchStrength(12)
                .setSeed(12345L);
        BufferedImage aged = SealUtil.buildPersonSeal(300, 16, baseFont, null, aging);
        require(!samePixels(base, aged), "private seal aging should change rendered pixels");
    }

    private static SealConfiguration config(int offsetX, int offsetY, boolean aging, long seed) {
        SealFont main = new SealFont()
                .setFontText("某某有限责任公司")
                .setFontFamily("宋体")
                .setFontSize(30)
                .setFontSpace(15.0)
                .setMarginSize(10)
                .setBold(true)
                .setOffsetX(offsetX)
                .setOffsetY(offsetY);

        SealFont center = new SealFont()
                .setFontText("123456789012345")
                .setFontFamily("宋体")
                .setFontSize(18)
                .setMarginSize(-2)
                .setBold(false)
                .setOffsetX(offsetX)
                .setOffsetY(offsetY);

        return new SealConfiguration()
                .setImageSize(340)
                .setBackgroudColor(Color.RED)
                .setMainFont(main)
                .setCenterFont(center)
                .setBorderCircle(new SealCircle(4, 150, 108))
                .setBorderInnerCircle(new SealCircle(1, 144, 102))
                .setAging(new SealAging()
                        .setEnabled(aging)
                        .setIntensity(35)
                        .setGrainSize(2)
                        .setDamageSize(4)
                        .setScratchStrength(12)
                        .setSeed(seed));
    }

    private static boolean samePixels(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean transparentBackgroundRemainsTransparent(BufferedImage clean, BufferedImage aged) {
        for (int y = 0; y < clean.getHeight(); y++) {
            for (int x = 0; x < clean.getWidth(); x++) {
                int cleanAlpha = (clean.getRGB(x, y) >>> 24) & 0xFF;
                int agedAlpha = (aged.getRGB(x, y) >>> 24) & 0xFF;
                if (cleanAlpha == 0 && agedAlpha != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
