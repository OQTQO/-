package cn.localhost01.seal.ui;

import cn.localhost01.seal.SealUtil;
import cn.localhost01.seal.configuration.SealCircle;
import cn.localhost01.seal.configuration.SealConfiguration;
import cn.localhost01.seal.configuration.SealFont;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

final class PublicSealPanel extends JPanel {
    private final Timer renderTimer = new Timer(100, event -> renderPreview());
    private final SealPreviewPanel preview = new SealPreviewPanel();
    private final JLabel status = new JLabel("准备就绪");
    private final JSpinner imageSize = spinner(300, 160, 1600, 10);
    private final JButton colorButton = new JButton("选择印章颜色");

    private final FontControls mainFont = new FontControls("主文字", true);
    private final FontControls viceFont = new FontControls("副文字", true);
    private final FontControls centerFont = new FontControls("中心文字", false);
    private final FontControls titleFont = new FontControls("抬头文字", false);

    private final CircleControls borderCircle = new CircleControls("外边线", false);
    private final CircleControls borderInnerCircle = new CircleControls("内边线", true);
    private final CircleControls innerCircle = new CircleControls("内环线", true);

    private Color sealColor = Color.RED;
    private BufferedImage currentImage;

    PublicSealPanel() {
        super(new BorderLayout());
        setBackground(SealStudioFrame.BACKGROUND);
        renderTimer.setRepeats(false);

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(new EmptyBorder(0, 0, 0, 10));

        controls.add(buildQuickPanel());
        controls.add(Box.createVerticalStrut(10));
        controls.add(buildGlobalPanel());
        controls.add(Box.createVerticalStrut(10));
        controls.add(mainFont.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(viceFont.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(centerFont.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(titleFont.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(borderCircle.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(borderInnerCircle.panel);
        controls.add(Box.createVerticalStrut(10));
        controls.add(innerCircle.panel);
        controls.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(controls);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setPreferredSize(new Dimension(440, 600));
        scroll.getViewport().setBackground(SealStudioFrame.BACKGROUND);

        JPanel previewCard = SealStudioFrame.card(new BorderLayout());
        JLabel previewTitle = new JLabel("实时预览");
        previewTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        previewTitle.setForeground(SealStudioFrame.TEXT);

        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setOpaque(false);
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
        split.setResizeWeight(0);
        split.setDividerLocation(445);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        bindAll();
        resetToUpstreamExample();
    }

    private JPanel buildQuickPanel() {
        JPanel panel = SealStudioFrame.card(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton upstream = new JButton("原项目示例");
        JButton round = new JButton("标准圆章");
        JButton oval = new JButton("椭圆章");

        upstream.addActionListener(event -> resetToUpstreamExample());
        round.addActionListener(event -> applyRoundPreset());
        oval.addActionListener(event -> applyOvalPreset());

        panel.add(upstream);
        panel.add(round);
        panel.add(oval);
        return panel;
    }

    private JPanel buildGlobalPanel() {
        JPanel panel = SealStudioFrame.card(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        addTitle(panel, gbc, "画布与颜色");
        addRow(panel, gbc, "输出尺寸", imageSize);

        colorButton.setBackground(sealColor);
        colorButton.setForeground(Color.WHITE);
        colorButton.setOpaque(true);
        colorButton.addActionListener(event -> {
            Color selected = JColorChooser.showDialog(this, "选择印章颜色", sealColor);
            if (selected != null) {
                sealColor = selected;
                colorButton.setBackground(selected);
                scheduleRender();
            }
        });
        addRow(panel, gbc, "印章颜色", colorButton);

        JLabel hint = new JLabel("背景透明；绘制仍由原 SealUtil.buildSeal() 完成");
        hint.setForeground(SealStudioFrame.MUTED);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hint, gbc);
        return panel;
    }

    private void bindAll() {
        imageSize.addChangeListener(event -> scheduleRender());
        mainFont.bind(this::scheduleRender);
        viceFont.bind(this::scheduleRender);
        centerFont.bind(this::scheduleRender);
        titleFont.bind(this::scheduleRender);
        borderCircle.bind(this::scheduleRender);
        borderInnerCircle.bind(this::scheduleRender);
        innerCircle.bind(this::scheduleRender);
    }

    private void scheduleRender() {
        renderTimer.restart();
    }

    private void renderPreview() {
        long started = System.nanoTime();
        try {
            SealConfiguration configuration = new SealConfiguration()
                    .setImageSize(intValue(imageSize))
                    .setBackgroudColor(sealColor)
                    .setMainFont(mainFont.toSealFont())
                    .setViceFont(viceFont.toSealFont())
                    .setCenterFont(centerFont.toSealFont())
                    .setTitleFont(titleFont.toSealFont())
                    .setBorderCircle(borderCircle.toSealCircle())
                    .setBorderInnerCircle(borderInnerCircle.toSealCircle())
                    .setInnerCircle(innerCircle.toSealCircle());

            currentImage = SealUtil.buildSeal(configuration);
            preview.setImage(currentImage);
            long millis = (System.nanoTime() - started) / 1_000_000;
            status.setForeground(SealStudioFrame.MUTED);
            status.setText(currentImage.getWidth() + " × " + currentImage.getHeight()
                    + " px · Graphics2D 实时渲染 · " + millis + " ms");
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
        chooser.setDialogTitle("导出印章 PNG");
        chooser.setSelectedFile(new File("seal.png"));
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

    private void resetToUpstreamExample() {
        imageSize.setValue(300);
        sealColor = Color.RED;
        colorButton.setBackground(sealColor);

        mainFont.set(true, "欢乐无敌制图网淘宝店专用章", "楷体", 25, 12.0, 10, true);
        viceFont.set(true, "正版认证", "宋体", 22, 12.0, 5, true);
        centerFont.set(true, "发货专用", "宋体", 25, 10.0, 0, true);
        titleFont.set(false, "正版认证", "宋体", 22, 10.0, 27, true);

        borderCircle.set(true, 3, 140, 100);
        borderInnerCircle.set(true, 1, 135, 95);
        innerCircle.set(true, 2, 85, 45);
        scheduleRender();
    }

    private void applyRoundPreset() {
        imageSize.setValue(320);
        mainFont.set(true, "某某有限责任公司", "宋体", 32, 28.0, 10, true);
        viceFont.set(true, "123456789012345", "宋体", 16, 18.0, 8, true);
        centerFont.set(true, "★", "宋体", 92, 10.0, -4, true);
        titleFont.set(false, "", "宋体", 22, 10.0, 0, true);
        borderCircle.set(true, 4, 145, 145);
        borderInnerCircle.set(false, 1, 138, 138);
        innerCircle.set(false, 2, 105, 105);
        scheduleRender();
    }

    private void applyOvalPreset() {
        imageSize.setValue(340);
        mainFont.set(true, "SEAL STUDIO COMPANY", "Microsoft YaHei UI", 24, 15.0, 10, true);
        viceFont.set(true, "专用章", "宋体", 22, 18.0, 8, true);
        centerFont.set(true, "★", "宋体", 76, 10.0, -3, true);
        titleFont.set(false, "", "宋体", 22, 10.0, 0, true);
        borderCircle.set(true, 4, 150, 108);
        borderInnerCircle.set(true, 1, 144, 102);
        innerCircle.set(false, 2, 90, 55);
        scheduleRender();
    }

    private static int intValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    private static JSpinner spinner(Number value, Comparable<?> min, Comparable<?> max, Number step) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    private static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private static void addTitle(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        label.setForeground(SealStudioFrame.TEXT);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, gbc);
        gbc.gridwidth = 1;
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

    private static final class FontControls {
        final JPanel panel = SealStudioFrame.card(new GridBagLayout());
        final JCheckBox enabled = new JCheckBox("启用");
        final JTextField text = new JTextField();
        final JComboBox<String> family = new JComboBox<>(SealFont.getSupportFontNames());
        final JSpinner size = spinner(25, 8, 180, 1);
        final JSpinner space = spinner(12.0, 0.0, 180.0, 0.5);
        final JSpinner margin = spinner(0, -160, 160, 1);
        final JCheckBox bold = new JCheckBox("加粗", true);
        final boolean allowSpace;

        FontControls(String title, boolean allowSpace) {
            this.allowSpace = allowSpace;
            GridBagConstraints gbc = baseGbc();
            addTitle(panel, gbc, title);

            JPanel toggles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            toggles.setOpaque(false);
            enabled.setOpaque(false);
            bold.setOpaque(false);
            toggles.add(enabled);
            toggles.add(Box.createHorizontalStrut(16));
            toggles.add(bold);
            addRow(panel, gbc, "状态", toggles);
            addRow(panel, gbc, "文字", text);
            family.setEditable(false);
            addRow(panel, gbc, "字体", family);
            addRow(panel, gbc, "字号", size);
            if (allowSpace) {
                addRow(panel, gbc, "字距 / 角跨度", space);
            }
            addRow(panel, gbc, "边距 / 偏移", margin);
        }

        void bind(Runnable callback) {
            enabled.addActionListener(event -> callback.run());
            bold.addActionListener(event -> callback.run());
            family.addActionListener(event -> callback.run());
            size.addChangeListener(event -> callback.run());
            space.addChangeListener(event -> callback.run());
            margin.addChangeListener(event -> callback.run());
            text.getDocument().addDocumentListener(documentListener(callback));
        }

        void set(boolean on, String value, String font, int fontSize, double fontSpace, int marginSize, boolean isBold) {
            enabled.setSelected(on);
            text.setText(value);
            family.setSelectedItem(font);
            size.setValue(fontSize);
            space.setValue(fontSpace);
            margin.setValue(marginSize);
            bold.setSelected(isBold);
        }

        SealFont toSealFont() {
            if (!enabled.isSelected() || text.getText().isEmpty()) {
                return null;
            }
            return new SealFont()
                    .setFontText(text.getText())
                    .setFontFamily(String.valueOf(family.getSelectedItem()))
                    .setFontSize(intValue(size))
                    .setFontSpace(((Number) space.getValue()).doubleValue())
                    .setMarginSize(intValue(margin))
                    .setBold(bold.isSelected());
        }
    }

    private static final class CircleControls {
        final JPanel panel = SealStudioFrame.card(new GridBagLayout());
        final JCheckBox enabled = new JCheckBox("启用", true);
        final JSpinner line = spinner(2, 1, 40, 1);
        final JSpinner width = spinner(140, 10, 760, 1);
        final JSpinner height = spinner(140, 10, 760, 1);
        final boolean optional;

        CircleControls(String title, boolean optional) {
            this.optional = optional;
            GridBagConstraints gbc = baseGbc();
            addTitle(panel, gbc, title);
            enabled.setOpaque(false);
            if (optional) {
                addRow(panel, gbc, "状态", enabled);
            }
            addRow(panel, gbc, "线宽", line);
            addRow(panel, gbc, "横向半径", width);
            addRow(panel, gbc, "纵向半径", height);
        }

        void bind(Runnable callback) {
            enabled.addActionListener(event -> callback.run());
            line.addChangeListener(event -> callback.run());
            width.addChangeListener(event -> callback.run());
            height.addChangeListener(event -> callback.run());
        }

        void set(boolean on, int lineSize, int radiusWidth, int radiusHeight) {
            enabled.setSelected(on);
            line.setValue(lineSize);
            width.setValue(radiusWidth);
            height.setValue(radiusHeight);
        }

        SealCircle toSealCircle() {
            if (optional && !enabled.isSelected()) {
                return null;
            }
            return new SealCircle(intValue(line), intValue(width), intValue(height));
        }
    }

    private static DocumentListener documentListener(Runnable callback) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                callback.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                callback.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                callback.run();
            }
        };
    }
}
