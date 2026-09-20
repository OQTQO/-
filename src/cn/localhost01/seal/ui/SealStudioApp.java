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

                Font normal = new Font("Microsoft YaHei UI", Font.PLAIN, 14);
                Font bold = new Font("Microsoft YaHei UI", Font.BOLD, 14);

                UIManager.put("Label.font", normal);
                UIManager.put("Button.font", normal);
                UIManager.put("CheckBox.font", normal);
                UIManager.put("RadioButton.font", normal);
                UIManager.put("TextField.font", normal);
                UIManager.put("ComboBox.font", normal);
                UIManager.put("Spinner.font", normal);
                UIManager.put("TabbedPane.font", bold);
                UIManager.put("TabbedPane.selected", new Color(248, 239, 239));
                UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
                UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            } catch (Exception ignored) {
            }

            SealStudioFrame frame = new SealStudioFrame();
            frame.setVisible(true);
        });
    }
}
