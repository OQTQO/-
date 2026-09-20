package cn.localhost01.seal.ui.model;

import cn.localhost01.seal.configuration.SealAging;
import cn.localhost01.seal.configuration.SealCircle;
import cn.localhost01.seal.configuration.SealConfiguration;
import cn.localhost01.seal.configuration.SealFont;

import java.awt.*;

public final class SealEditorState {
    public int imageSize = 340;
    public Color color = Color.RED;

    public final TextState main = new TextState(true, "某某有限责任公司", "宋体", 32, 28.0, 10, true);
    public final TextState vice = new TextState(true, "发票专用章", "宋体", 20, 18.0, 8, true);
    public final TextState center = new TextState(true, "123456789012345", "宋体", 18, 10.0, -2, false);
    public final TextState title = new TextState(false, "", "宋体", 22, 10.0, 0, true);

    public final CircleState border = new CircleState(true, 4, 300, 216);
    public final CircleState borderInner = new CircleState(true, 1, 288, 204);
    public final CircleState inner = new CircleState(false, 2, 180, 110);

    public final SealAging aging = new SealAging()
            .setEnabled(false)
            .setIntensity(35)
            .setGrainSize(2)
            .setDamageSize(4)
            .setScratchStrength(10)
            .setSeed(43821L);

    public SealConfiguration toConfiguration() {
        return new SealConfiguration()
                .setImageSize(imageSize)
                .setBackgroudColor(color)
                .setMainFont(main.toFont(true))
                .setViceFont(vice.toFont(true))
                .setCenterFont(center.toFont(false))
                .setTitleFont(title.toFont(false))
                .setBorderCircle(border.toCircle(false))
                .setBorderInnerCircle(borderInner.toCircle(true))
                .setInnerCircle(inner.toCircle(true))
                .setAging(aging);
    }

    public void applyOvalPreset() {
        imageSize = 340;
        color = Color.RED;
        main.set(true, "某某有限责任公司", "宋体", 32, 15.0, 10, true, 0, 0);
        vice.set(true, "发票专用章", "宋体", 22, 18.0, 8, true, 0, 0);
        center.set(true, "123456789012345", "宋体", 18, 10.0, -2, false, 0, 0);
        title.set(false, "", "宋体", 22, 10.0, 0, true, 0, 0);
        border.set(true, 4, 300, 216);
        borderInner.set(true, 1, 288, 204);
        inner.set(false, 2, 180, 110);
        resetAging();
    }

    public void applyRoundPreset() {
        imageSize = 320;
        color = Color.RED;
        main.set(true, "某某有限责任公司", "宋体", 32, 28.0, 10, true, 0, 0);
        vice.set(true, "123456789012345", "宋体", 16, 18.0, 8, true, 0, 0);
        center.set(true, "★", "宋体", 92, 10.0, -4, true, 0, 0);
        title.set(false, "", "宋体", 22, 10.0, 0, true, 0, 0);
        border.set(true, 4, 290, 290);
        borderInner.set(false, 1, 276, 276);
        inner.set(false, 2, 210, 210);
        resetAging();
    }

    public void applyUpstreamExample() {
        imageSize = 300;
        color = Color.RED;
        main.set(true, "欢乐无敌制图网淘宝店专用章", "楷体", 25, 12.0, 10, true, 0, 0);
        vice.set(true, "正版认证", "宋体", 22, 12.0, 5, true, 0, 0);
        center.set(true, "发货专用", "宋体", 25, 10.0, 0, true, 0, 0);
        title.set(false, "正版认证", "宋体", 22, 10.0, 27, true, 0, 0);
        border.set(true, 3, 280, 200);
        borderInner.set(true, 1, 270, 190);
        inner.set(true, 2, 170, 90);
        resetAging();
    }

    public void resetAging() {
        aging.setEnabled(false)
                .setIntensity(35)
                .setGrainSize(2)
                .setDamageSize(4)
                .setScratchStrength(10)
                .setOffsetX(0)
                .setOffsetY(0)
                .setSeed(43821L);
    }

    public static final class TextState {
        public boolean enabled;
        public String text;
        public String family;
        public int size;
        public double space;
        public int margin;
        public boolean bold;
        public int offsetX;
        public int offsetY;

        public TextState(boolean enabled, String text, String family, int size, double space, int margin, boolean bold) {
            set(enabled, text, family, size, space, margin, bold, 0, 0);
        }

        public void set(boolean enabled, String text, String family, int size, double space, int margin,
                boolean bold, int offsetX, int offsetY) {
            this.enabled = enabled;
            this.text = text;
            this.family = family;
            this.size = size;
            this.space = space;
            this.margin = margin;
            this.bold = bold;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public SealFont toFont(boolean arc) {
            if (!enabled || text == null || text.isBlank()) {
                return null;
            }
            SealFont font = new SealFont()
                    .setFontText(text)
                    .setFontFamily(family)
                    .setFontSize(size)
                    .setMarginSize(margin)
                    .setBold(bold)
                    .setOffsetX(offsetX)
                    .setOffsetY(offsetY);
            if (arc) {
                font.setFontSpace(space);
            } else {
                // Keep a non-null value for private-seal reuse and compatibility.
                font.setFontSpace(space);
            }
            return font;
        }
    }

    public static final class CircleState {
        public boolean enabled;
        public int lineSize;
        public int width;
        public int height;

        public CircleState(boolean enabled, int lineSize, int width, int height) {
            set(enabled, lineSize, width, height);
        }

        public void set(boolean enabled, int lineSize, int width, int height) {
            this.enabled = enabled;
            this.lineSize = lineSize;
            this.width = width;
            this.height = height;
        }

        public SealCircle toCircle(boolean optional) {
            if (optional && !enabled) {
                return null;
            }
            return new SealCircle(lineSize, Math.max(1, width / 2), Math.max(1, height / 2));
        }
    }
}
