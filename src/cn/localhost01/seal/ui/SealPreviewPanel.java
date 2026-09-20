package cn.localhost01.seal.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

final class SealPreviewPanel extends JPanel {
    private BufferedImage image;

    SealPreviewPanel() {
        setOpaque(true);
        setBackground(new Color(246, 247, 249));
        setPreferredSize(new Dimension(560, 560));
    }

    void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            paintCheckerboard(g);
            if (image == null) {
                g.setColor(new Color(122, 129, 139));
                g.setFont(getFont().deriveFont(Font.PLAIN, 15f));
                String text = "调整左侧参数后，这里会实时显示印章";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
                return;
            }

            int padding = 48;
            double scale = Math.min(
                    (getWidth() - padding * 2.0) / image.getWidth(),
                    (getHeight() - padding * 2.0) / image.getHeight());
            scale = Math.min(scale, 1.8);
            int w = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(image, x, y, w, h, null);
        } finally {
            g.dispose();
        }
    }

    private void paintCheckerboard(Graphics2D g) {
        int cell = 16;
        Color a = new Color(250, 250, 250);
        Color b = new Color(235, 237, 240);
        for (int y = 0; y < getHeight(); y += cell) {
            for (int x = 0; x < getWidth(); x += cell) {
                g.setColor((((x / cell) + (y / cell)) & 1) == 0 ? a : b);
                g.fillRect(x, y, cell, cell);
            }
        }
    }
}
