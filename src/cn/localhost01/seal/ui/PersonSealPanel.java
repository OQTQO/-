package cn.localhost01.seal.ui;

import cn.localhost01.seal.SealUtil;
import cn.localhost01.seal.configuration.SealFont;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

final class PersonSealPanel extends JPanel {
    private final Timer renderTimer = new Timer(100, event -> renderPreview());
    private final SealPreviewPanel preview = new SealPreviewPanel();
    private final JLabel status = new JLabel("准备就绪");

    private final JTextField text = new JTextField("诸葛孔明");
    private final JTextField addText = new JTextField("印");
    private final JComboBox<String> family = new JComboBox<>(SealFont.getSupportFontNames());
    private final JSpinner imageSize = spinner(300, 100, 1000, 10);
    private final JSpinner lineSize = spinner(16, 1, 80, 1);
    private final JSpinner fontSize = spinner(120, 16, 320, 1);
    private final JSpinner fontSpace = spinner(10.0, 0.0, 100.0, 0.5);
    private final JCheckBox bold = new JCheckBox("加粗", true);

    private BufferedImage currentImage;

    PersonSealPanel() {
        super(new BorderLayout());
        setBackground(SealStudioFrame.BACKGROUND);
        renderTimer.setRepeats(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(0, 0, 0, 10));
        left.add(buildControls());
        left.add(Box.createVerticalStrut(10));
        left.add(buildNotes());
        left.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(left);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(440, 600));
        scroll.getViewport().setBackground(SealStudioFrame.BACKGROUND);

        JPanel previewCard = SealStudioFrame.card(new BorderLayout());
        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setOpaque(false);

        JLabel previewTitle = new JLabel("实时预览");
        previewTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        previewTitle.setForeground(SealStudioFrame.TEXT);
        previewHeader.add(previewTitle, BorderLayout.WEST);

        JButton export = SealStudioFrame.primaryButton("导出 PNG");
        export.addActionListener(event -> exportPng());
        previewHeader.add(export, BorderLayout.EAST);

        status.setForeground(SealStudioFrame.MUTED);
        status.setBorder(new EmptyBorder(8, 2, 0, 2));

        previewCard.add(previewHeader, BorderLayout.NORTH);
        previewCard.add(preview, BorderLayout.CENTER);
        previewCard.add(status, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, previewCard);
        split.setDividerLocation(445);
        split.setResizeWeight(0);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        bind();
        family.setSelectedItem("宋体");
        scheduleRender();
    }

    private JPanel buildControls() {
        JPanel panel = SealStudioFrame.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("私章参数");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        title.setForeground(SealStudioFrame.TEXT);
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        addRow(panel, gbc, "姓名文字", text);
        addRow(panel, gbc, "追加字符", addText);
        addRow(panel, gbc, "字体", family);
        addRow(panel, gbc, "字号", fontSize);
        addRow(panel, gbc, "字距", fontSpace);
        addRow(panel, gbc, "边框线宽", lineSize);
        addRow(panel, gbc, "输出尺寸", imageSize);

        bold.setOpaque(false);
        addRow(panel, gbc, "样式", bold);

        JButton reset = new JButton("恢复原项目示例");
        reset.addActionListener(event -> {
            text.setText("诸葛孔明");
            addText.setText("印");
            family.setSelectedItem("宋体");
            fontSize.setValue(120);
            fontSpace.setValue(10.0);
            lineSize.setValue(16);
            imageSize.setValue(300);
            bold.setSelected(true);
            scheduleRender();
        });
        addRow(panel, gbc, "", reset);
        return panel;
    }

    private JPanel buildNotes() {
        JPanel panel = SealStudioFrame.card(new BorderLayout());
        JLabel note = new JLabel(
                "<html><b>原绘制规则</b><br><br>"
                        + "姓名必须为 2～4 个字符；追加字符由原 buildPersonSeal() 规则处理。"
                        + "<br>私章颜色在原核心中固定为红色，因此 UI 不额外修改颜色算法。"
                        + "<br>所有预览均直接调用 SealUtil.buildPersonSeal()。</html>");
        note.setForeground(SealStudioFrame.MUTED);
        panel.add(note, BorderLayout.CENTER);
        return panel;
    }

    private void bind() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                scheduleRender();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                scheduleRender();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                scheduleRender();
            }
        };
        text.getDocument().addDocumentListener(listener);
        addText.getDocument().addDocumentListener(listener);
        family.addActionListener(event -> scheduleRender());
        imageSize.addChangeListener(event -> scheduleRender());
        lineSize.addChangeListener(event -> scheduleRender());
        fontSize.addChangeListener(event -> scheduleRender());
        fontSpace.addChangeListener(event -> scheduleRender());
        bold.addActionListener(event -> scheduleRender());
    }

    private void scheduleRender() {
        renderTimer.restart();
    }

    private void renderPreview() {
        long started = System.nanoTime();
        try {
            SealFont font = new SealFont()
                    .setFontText(text.getText())
                    .setFontFamily(String.valueOf(family.getSelectedItem()))
                    .setFontSize(intValue(fontSize))
                    .setFontSpace(((Number) fontSpace.getValue()).doubleValue())
                    .setBold(bold.isSelected());

            String extra = addText.getText().isBlank() ? null : addText.getText();
            currentImage = SealUtil.buildPersonSeal(
                    intValue(imageSize),
                    intValue(lineSize),
                    font,
                    extra);

            preview.setImage(currentImage);
            long millis = (System.nanoTime() - started) / 1_000_000;
            status.setForeground(SealStudioFrame.MUTED);
            status.setText(currentImage.getWidth() + " × " + currentImage.getHeight()
                    + " px · 原 buildPersonSeal() · " + millis + " ms");
        } catch (Exception exception) {
            currentImage = null;
            preview.setImage(null);
            status.setForeground(new Color(183, 28, 28));
            status.setText("参数暂时无法渲染：" + exception.getMessage());
        }
    }

    private void exportPng() {
        if (currentImage == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出私章 PNG");
        chooser.setSelectedFile(new File("person-seal.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }

        try {
            ImageIO.write(currentImage, "png", file);
            status.setForeground(new Color(34, 122, 63));
            status.setText("已导出：" + file.getAbsolutePath());
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "导出失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, String name, JComponent component) {
        JLabel label = new JLabel(name);
        label.setForeground(SealStudioFrame.MUTED);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private static JSpinner spinner(Number value, Comparable<?> min, Comparable<?> max, Number step) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    private static int intValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }
}
