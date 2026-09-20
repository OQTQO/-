package cn.localhost01.seal.effect;

import cn.localhost01.seal.configuration.SealAging;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 对已经绘制完成的透明印章做确定性的印泥老化后处理。
 * 只降低已有印泥像素的 alpha，不向透明背景新增颜色。
 */
public final class SealAgingEffect {
    private SealAgingEffect() {
    }

    public static BufferedImage apply(BufferedImage source, SealAging config) {
        if (source == null || config == null || !config.isEnabled() || config.getIntensity() <= 0) {
            return source;
        }

        BufferedImage result = copy(source);
        double scale = Math.max(0.35, Math.min(source.getWidth(), source.getHeight()) / 300.0);
        int grain = Math.max(1, (int) Math.round(config.getGrainSize() * scale));
        int damage = Math.max(1, (int) Math.round(config.getDamageSize() * scale));
        int intensity = config.getIntensity();

        Random random = new Random(config.getSeed());
        int area = source.getWidth() * source.getHeight();
        int grainCount = Math.max(1, (int) (area * (0.00010 + intensity * 0.000012)));
        int damageCount = Math.max(1, (int) (area * (0.000010 + intensity * 0.0000022)));
        int scratchCount = (int) Math.round(config.getScratchStrength() * (0.03 + intensity * 0.002));

        for (int i = 0; i < grainCount; i++) {
            int x = random.nextInt(source.getWidth()) + config.getOffsetX();
            int y = random.nextInt(source.getHeight()) + config.getOffsetY();
            int radius = Math.max(1, grain + random.nextInt(Math.max(1, grain + 1)) / 2);
            eraseSoftCircle(result, x, y, radius, 0.25 + random.nextDouble() * 0.65, random);
        }

        for (int i = 0; i < damageCount; i++) {
            int x = random.nextInt(source.getWidth()) + config.getOffsetX();
            int y = random.nextInt(source.getHeight()) + config.getOffsetY();
            int rx = Math.max(1, damage + random.nextInt(Math.max(1, damage + 1)));
            int ry = Math.max(1, damage / 2 + random.nextInt(Math.max(1, damage)));
            eraseEllipse(result, x, y, rx, ry, 0.55 + random.nextDouble() * 0.45, random);
        }

        for (int i = 0; i < scratchCount; i++) {
            int x = random.nextInt(source.getWidth()) + config.getOffsetX();
            int y = random.nextInt(source.getHeight()) + config.getOffsetY();
            double angle = random.nextDouble() * Math.PI * 2.0;
            int length = Math.max(3, (int) Math.round((3 + random.nextInt(13)) * scale));
            int thickness = Math.max(1, (int) Math.round(scale));
            int x2 = x + (int) Math.round(Math.cos(angle) * length);
            int y2 = y + (int) Math.round(Math.sin(angle) * length);
            eraseLine(result, x, y, x2, y2, thickness, 0.45 + random.nextDouble() * 0.55);
        }

        return result;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = result.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return result;
    }

    private static void eraseSoftCircle(BufferedImage image, int cx, int cy, int radius, double strength, Random random) {
        int r2 = radius * radius;
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy <= r2) {
                    double distance = Math.sqrt(dx * dx + dy * dy) / Math.max(1.0, radius);
                    double local = strength * (1.0 - distance * 0.35) * (0.75 + random.nextDouble() * 0.25);
                    reduceAlpha(image, x, y, local);
                }
            }
        }
    }

    private static void eraseEllipse(BufferedImage image, int cx, int cy, int rx, int ry, double strength, Random random) {
        for (int y = cy - ry; y <= cy + ry; y++) {
            for (int x = cx - rx; x <= cx + rx; x++) {
                double nx = (x - cx) / (double) Math.max(1, rx);
                double ny = (y - cy) / (double) Math.max(1, ry);
                if (nx * nx + ny * ny <= 1.0 && random.nextDouble() > 0.12) {
                    reduceAlpha(image, x, y, strength * (0.72 + random.nextDouble() * 0.28));
                }
            }
        }
    }

    private static void eraseLine(BufferedImage image, int x0, int y0, int x1, int y1, int thickness, double strength) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;

        while (true) {
            for (int oy = -thickness; oy <= thickness; oy++) {
                for (int ox = -thickness; ox <= thickness; ox++) {
                    reduceAlpha(image, x0 + ox, y0 + oy, strength);
                }
            }
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * error;
            if (e2 >= dy) {
                error += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                error += dx;
                y0 += sy;
            }
        }
    }

    private static void reduceAlpha(BufferedImage image, int x, int y, double strength) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return;
        }
        int argb = image.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return;
        }
        int newAlpha = (int) Math.round(alpha * Math.max(0.0, 1.0 - Math.min(1.0, strength)));
        image.setRGB(x, y, (newAlpha << 24) | (argb & 0x00FFFFFF));
    }
}
