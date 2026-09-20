package cn.localhost01.seal.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class SealStudioFrame extends JFrame {
    static final Color ACCENT = new Color(194, 42, 42);
    static final Color TEXT = new Color(36, 40, 48);
    static final Color MUTED = new Color(106, 113, 124);
    static final Color SURFACE = Color.WHITE;
    static final Color BACKGROUND = new Color(242, 244, 247);

    SealStudioFrame() {
        super("SealStudio · 印章设计器");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setSize(1320, 860);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(18, 24, 14, 24));

        JLabel title = new JLabel("SealStudio");
        title.setForeground(TEXT);
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("基于原 SealUtil / Java Graphics2D 绘制核心 · 参数调整即时预览");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(12, 18, 18, 18));
        tabs.addTab("公章 / 圆章", new PublicSealPanel());
        tabs.addTab("私章", new PersonSealPanel());

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 227, 232)),
                new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        return button;
    }
}
