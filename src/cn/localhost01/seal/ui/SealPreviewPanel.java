package cn.localhost01.seal.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

final class SealPreviewPanel extends JPanel {
    private BufferedImage image;
    private boolean showGuides;
    private double zoom = 1.0;
    private int drawX;
    private int drawY;
    private int drawW;
    private int drawH;
    private double drawScale = 1.0;
    private Point lastDragPoint;
    private BiConsumer<Integer, Integer> dragListener;

    SealPreviewPanel() {
        setOpaque(true);
        setBackground(new Color(246, 247, 249));
        setPreferredSize(new Dimension(560, 560));

        MouseAdapter drag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (image != null && containsImagePoint(e.getPoint())) {
                    lastDragPoint = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint == null || dragListener == null || drawScale <= 0) {
                    return;
                }
                int dx = (int) Math.round((e.getX() - lastDragPoint.x) / drawScale);
                int dy = (int) Math.round((e.getY() - lastDragPoint.y) / drawScale);
                if (dx != 0 || dy != 0) {
                    dragListener.accept(dx, dy);
                    lastDragPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
                setCursor(Cursor.getDefaultCursor());
            }
        };
        addMouseListener(drag);
        addMouseMotionListener(drag);
    }

    void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    void setShowGuides(boolean showGuides) {
        this.showGuides = showGuides;
        repaint();
    }

    void setZoom(double zoom) {
        this.zoom = Math.max(0.5, Math.min(3.0, zoom));
        repaint();
    }

    double getZoom() {
        return zoom;
    }

    void setDragListener(BiConsumer<Integer, Integer> dragListener) {
        this.dragListener = dragListener;
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
                String text = "调整参数后，这里会实时显示印章";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
                return;
            }

            int padding = 42;
            double fitScale = Math.min(
                    Math.max(1, getWidth() - padding * 2.0) / image.getWidth(),
                    Math.max(1, getHeight() - padding * 2.0) / image.getHeight());
            drawScale = Math.max(0.1, fitScale * zoom);
            drawW = Math.max(1, (int) Math.round(image.getWidth() * drawScale));
            drawH = Math.max(1, (int) Math.round(image.getHeight() * drawScale));
            drawX = (getWidth() - drawW) / 2;
            drawY = (getHeight() - drawH) / 2;

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(image, drawX, drawY, drawW, drawH, null);

            if (showGuides) {
                g.setColor(new Color(80, 112, 150, 105));
                g.setStroke(new BasicStroke(1f));
                int cx = drawX + drawW / 2;
                int cy = drawY + drawH / 2;
                g.drawLine(drawX, cy, drawX + drawW, cy);
                g.drawLine(cx, drawY, cx, drawY + drawH);
                g.setColor(new Color(80, 112, 150, 150));
                g.drawOval(cx - 3, cy - 3, 6, 6);
            }
        } finally {
            g.dispose();
        }
    }

    private boolean containsImagePoint(Point point) {
        return point.x >= drawX && point.x <= drawX + drawW
                && point.y >= drawY && point.y <= drawY + drawH;
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
