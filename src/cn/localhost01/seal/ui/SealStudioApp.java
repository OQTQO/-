package cn.localhost01.seal.ui;

import javax.swing.*;
import java.awt.*;

public final class SealStudioApp {
    private SealStudioApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Label.font", new Font("Microsoft YaHei UI", Font.PLAIN, 13));
                UIManager.put("Button.font", new Font("Microsoft YaHei UI", Font.PLAIN, 13));
                UIManager.put("TextField.font", new Font("Microsoft YaHei UI", Font.PLAIN, 13));
                UIManager.put("ComboBox.font", new Font("Microsoft YaHei UI", Font.PLAIN, 13));
                UIManager.put("Spinner.font", new Font("Microsoft YaHei UI", Font.PLAIN, 13));
                UIManager.put("TabbedPane.font", new Font("Microsoft YaHei UI", Font.BOLD, 14));
            } catch (Exception ignored) {
            }

            SealStudioFrame frame = new SealStudioFrame();
            frame.setVisible(true);
        });
    }
}
