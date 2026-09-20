package cn.localhost01.seal.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class SealStudioFrame extends JFrame {
    static final Color ACCENT = new Color(194, 42, 42);
    static final Color ACCENT_SOFT = new Color(250, 239, 239);
    static final Color TEXT = new Color(31, 35, 43);
    static final Color MUTED = new Color(104, 111, 123);
    static final Color SURFACE = Color.WHITE;
    static final Color BACKGROUND = new Color(244, 246, 249);
    static final Color BORDER = new Color(226, 229, 234);

    SealStudioFrame() {
        super("SealStudio V2 · 印章设计器");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 780));
        setSize(1420, 900);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(18, 26, 16, 26)));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SealStudio");
        title.setForeground(TEXT);
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 25));

        JLabel subtitle = new JLabel("可视化印章设计器 · 原 SealUtil Graphics2D 排版 + XY 精调 + 印泥老化");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));

        brand.add(title);
        brand.add(Box.createVerticalStrut(4));
        brand.add(subtitle);
        header.add(brand, BorderLayout.WEST);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        badges.setOpaque(false);
        badges.add(badge("实时预览"));
        badges.add(badge("XY 微调"));
        badges.add(badge("老化效果"));
        header.add(badges, BorderLayout.EAST);

        JTabbedPane modes = new JTabbedPane();
        modes.setBorder(new EmptyBorder(12, 18, 18, 18));
        modes.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        modes.addTab("公章设计", new PublicSealPanel());
        modes.addTab("私章设计", new PersonSealPanel());

        root.add(header, BorderLayout.NORTH);
        root.add(modes, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JLabel badge(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT);
        label.setBackground(ACCENT_SOFT);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(6, 10, 6, 10));
        return label;
    }

    static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 16, 9, 16));
        return button;
    }
}
