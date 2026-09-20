package cn.localhost01.seal.ui;

import cn.localhost01.seal.SealUtil;
import cn.localhost01.seal.configuration.SealFont;
import cn.localhost01.seal.ui.components.NumberStepper;
import cn.localhost01.seal.ui.components.PositionControl;
import cn.localhost01.seal.ui.model.PersonSealState;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

final class PersonSealPanel extends JPanel {
    private final PersonSealState state = new PersonSealState();
    private final SealPreviewPanel preview = new SealPreviewPanel();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel status = new JLabel("准备就绪");
    private final Timer renderTimer = new Timer(110, e -> renderPreview());
    private final AtomicInteger generation = new AtomicInteger();

    private final BasicEditor basicEditor = new BasicEditor();
    private final AgingEditor agingEditor = new AgingEditor();
    private final TextEditor textEditor = new TextEditor();
    private final BorderEditor borderEditor = new BorderEditor();

    private BufferedImage currentImage;

    PersonSealPanel() {
        super(new BorderLayout(14, 0));
        setBackground(SealStudioFrame.BACKGROUND);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        renderTimer.setRepeats(false);

        tabs.addTab("基础", wrap(basicEditor));
        tabs.addTab("老化", wrap(agingEditor));
        tabs.addTab("文字", wrap(textEditor));
        tabs.addTab("边框", wrap(borderEditor));
        tabs.addChangeListener(e -> syncVisibleEditor());

        JPanel editorCard = SealStudioFrame.card(new BorderLayout());
        editorCard.add(tabs, BorderLayout.CENTER);

        JPanel previewCard = buildPreviewCard();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorCard, previewCard);
        split.setBorder(null);
        split.setResizeWeight(0.66);
        split.setDividerLocation(770);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);

        preview.setDragListener((dx, dy) -> {
            if (tabs.getSelectedIndex() == 2) {
                textEditor.nudge(dx, dy);
            }
        });
        installKeyboardNudging();

        loadAll();
        scheduleRender();
    }

    private JPanel buildPreviewCard() {
        JPanel card = SealStudioFrame.card(new BorderLayout(0, 10));

        JLabel title = new JLabel("实时预览");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        title.setForeground(SealStudioFrame.TEXT);

        JButton export = SealStudioFrame.primaryButton("导出 PNG");
        export.addActionListener(e -> exportPng());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(export, BorderLayout.EAST);

        JCheckBox guides = new JCheckBox("辅助线");
        guides.setOpaque(false);
        guides.addActionListener(e -> preview.setShowGuides(guides.isSelected()));

        JComboBox<String> zoom = new JComboBox<>(new String[]{"50%", "75%", "100%", "125%", "150%", "200%"});
        zoom.setSelectedItem("100%");
        zoom.addActionListener(e -> {
            String value = String.valueOf(zoom.getSelectedItem()).replace("%", "");
            preview.setZoom(Double.parseDouble(value) / 100.0);
        });

        JLabel dragHint = new JLabel("切到“文字”后可直接拖动文字");
        dragHint.setForeground(SealStudioFrame.MUTED);

        JPanel controls = transparentFlow();
        controls.add(guides);
        controls.add(new JLabel("缩放"));
        controls.add(zoom);
        controls.add(dragHint);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(controls, BorderLayout.NORTH);
        status.setForeground(SealStudioFrame.MUTED);
        status.setBorder(new EmptyBorder(8, 2, 0, 2));
        bottom.add(status, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(preview, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JScrollPane wrap(JComponent component) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(SealStudioFrame.BACKGROUND);
        holder.setBorder(new EmptyBorder(14, 14, 14, 14));
        holder.add(component, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SealStudioFrame.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private void syncVisibleEditor() {
        switch (tabs.getSelectedIndex()) {
            case 0 -> basicEditor.load();
            case 1 -> agingEditor.load();
            case 2 -> textEditor.load();
            case 3 -> borderEditor.load();
            default -> {
            }
        }
    }

    private void loadAll() {
        basicEditor.load();
        agingEditor.load();
        textEditor.load();
        borderEditor.load();
    }

    private void installKeyboardNudging() {
        bindMove("LEFT", -1, 0);
        bindMove("RIGHT", 1, 0);
        bindMove("UP", 0, -1);
        bindMove("DOWN", 0, 1);
        bindMove("shift LEFT", -5, 0);
        bindMove("shift RIGHT", 5, 0);
        bindMove("shift UP", 0, -5);
        bindMove("shift DOWN", 0, 5);
    }

    private void bindMove(String keyStroke, int dx, int dy) {
        String key = "private-move-" + keyStroke;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (tabs.getSelectedIndex() == 2) {
                    textEditor.nudge(dx, dy);
                }
            }
        });
    }

    private void scheduleRender() {
        renderTimer.restart();
    }

    private void renderPreview() {
        final int id = generation.incrementAndGet();
        final int imageSize = state.imageSize;
        final int lineSize = state.lineSize;
        final SealFont font = state.toFont();
        final String extra = state.addText == null || state.addText.isBlank() ? null : state.addText;
        final var aging = state.copyAging();
        final long started = System.nanoTime();

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return SealUtil.buildPersonSeal(imageSize, lineSize, font, extra, aging);
            }

            @Override
            protected void done() {
                if (id != generation.get()) {
                    return;
                }
                try {
                    currentImage = get();
                    preview.setImage(currentImage);
                    long millis = (System.nanoTime() - started) / 1_000_000;
                    status.setForeground(SealStudioFrame.MUTED);
                    status.setText(currentImage.getWidth() + " x " + currentImage.getHeight()
                            + " px · buildPersonSeal · " + millis + " ms"
                            + (state.aging.isEnabled() ? " · 老化已开启" : ""));
                } catch (Exception exception) {
                    currentImage = null;
                    preview.setImage(null);
                    status.setForeground(new Color(183, 28, 28));
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    status.setText("参数暂时无法渲染：" + cause.getMessage());
                }
            }
        }.execute();
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

    private final class BasicEditor extends JPanel {
        private final JTextField text = new JTextField();
        private final JTextField addText = new JTextField();
        private final NumberStepper imageSize = new NumberStepper(300, 100, 1000, 10);
        private boolean loading;

        BasicEditor() {
            super(new GridBagLayout());
            setOpaque(false);
            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, "私章基础", "姓名支持 2～4 个字符；追加字符继续遵循原 buildPersonSeal 规则。");
            addRow(this, gbc, "姓名文字", text);
            addRow(this, gbc, "追加字符", addText);
            addRow(this, gbc, "输出尺寸", imageSize);

            JButton reset = new JButton("恢复原项目示例");
            reset.addActionListener(e -> {
                state.reset();
                loadAll();
                scheduleRender();
            });
            addRow(this, gbc, "模板", reset);

            text.getDocument().addDocumentListener(documentListener(() -> {
                if (!loading) {
                    state.text = text.getText();
                    scheduleRender();
                }
            }));
            addText.getDocument().addDocumentListener(documentListener(() -> {
                if (!loading) {
                    state.addText = addText.getText();
                    scheduleRender();
                }
            }));
            imageSize.addChangeListener(e -> {
                if (!loading) {
                    state.imageSize = imageSize.intValue();
                    scheduleRender();
                }
            });
        }

        void load() {
            loading = true;
            text.setText(state.text);
            addText.setText(state.addText);
            imageSize.setValue(state.imageSize);
            loading = false;
        }
    }

    private final class TextEditor extends JPanel {
        private final JComboBox<String> family = new JComboBox<>(SealFont.getSupportFontNames());
        private final NumberStepper size = new NumberStepper(120, 16, 320, 1);
        private final NumberStepper spacing = new NumberStepper(10.0, 0.0, 100.0, 0.5);
        private final JCheckBox bold = new JCheckBox("加粗", true);
        private final PositionControl position = new PositionControl();
        private boolean loading;

        TextEditor() {
            super(new GridBagLayout());
            setOpaque(false);
            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, "私章文字", "边框保持固定，XY 只移动私章文字内容。");
            bold.setOpaque(false);
            addRow(this, gbc, "字体", family);
            addRow(this, gbc, "字号", size);
            addRow(this, gbc, "字距", spacing);
            addRow(this, gbc, "字重", bold);

            addSubheading(this, gbc, "位置微调");
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(position, gbc);

            family.addActionListener(e -> updateModel());
            size.addChangeListener(e -> updateModel());
            spacing.addChangeListener(e -> updateModel());
            bold.addActionListener(e -> updateModel());
            position.setPositionListener((x, y) -> updateModel());
        }

        private void updateModel() {
            if (loading) return;
            state.family = String.valueOf(family.getSelectedItem());
            state.fontSize = size.intValue();
            state.fontSpace = spacing.doubleValue();
            state.bold = bold.isSelected();
            state.offsetX = position.getXOffset();
            state.offsetY = position.getYOffset();
            scheduleRender();
        }

        void load() {
            loading = true;
            family.setSelectedItem(state.family);
            size.setValue(state.fontSize);
            spacing.setValue(state.fontSpace);
            bold.setSelected(state.bold);
            position.setPosition(state.offsetX, state.offsetY);
            loading = false;
        }

        void nudge(int dx, int dy) {
            position.setPosition(
                    Math.max(-500, Math.min(500, state.offsetX + dx)),
                    Math.max(-500, Math.min(500, state.offsetY + dy)));
        }
    }

    private final class BorderEditor extends JPanel {
        private final NumberStepper line = new NumberStepper(16, 1, 80, 1);
        private boolean loading;

        BorderEditor() {
            super(new GridBagLayout());
            setOpaque(false);
            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, "边框", "私章边框继续由原 buildPersonSeal 绘制。");
            addRow(this, gbc, "边框线宽", line);
            line.addChangeListener(e -> {
                if (!loading) {
                    state.lineSize = line.intValue();
                    scheduleRender();
                }
            });
        }

        void load() {
            loading = true;
            line.setValue(state.lineSize);
            loading = false;
        }
    }

    private final class AgingEditor extends JPanel {
        private final JCheckBox enabled = new JCheckBox("开启老化");
        private final NumberStepper intensity = new NumberStepper(35, 0, 100, 1);
        private final NumberStepper grain = new NumberStepper(2, 1, 12, 1);
        private final NumberStepper damage = new NumberStepper(4, 1, 24, 1);
        private final NumberStepper scratches = new NumberStepper(10, 0, 100, 1);
        private final PositionControl position = new PositionControl();
        private final JSpinner seed = new JSpinner(new SpinnerNumberModel(43821L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));
        private boolean loading;

        AgingEditor() {
            super(new GridBagLayout());
            setOpaque(false);
            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, "印泥老化", "使用固定纹理种子，预览与导出保持一致。");
            enabled.setOpaque(false);
            addRow(this, gbc, "状态", enabled);
            addRow(this, gbc, "老化程度", intensity);
            addRow(this, gbc, "颗粒大小", grain);
            addRow(this, gbc, "缺损尺寸", damage);
            addRow(this, gbc, "划痕强度", scratches);
            addRow(this, gbc, "纹理种子", seed);

            addSubheading(this, gbc, "纹理位置");
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(position, gbc);

            JPanel presets = transparentFlow();
            JButton light = new JButton("轻微");
            JButton natural = new JButton("自然");
            JButton strong = new JButton("明显");
            JButton reroll = new JButton("重新生成纹理");
            JButton clear = new JButton("恢复无老化");
            presets.add(light);
            presets.add(natural);
            presets.add(strong);
            presets.add(reroll);
            presets.add(clear);
            addRow(this, gbc, "预设", presets);

            enabled.addActionListener(e -> updateModel());
            intensity.addChangeListener(e -> updateModel());
            grain.addChangeListener(e -> updateModel());
            damage.addChangeListener(e -> updateModel());
            scratches.addChangeListener(e -> updateModel());
            seed.addChangeListener(e -> updateModel());
            position.setPositionListener((x, y) -> updateModel());

            light.addActionListener(e -> applyPreset(18, 1, 2, 5));
            natural.addActionListener(e -> applyPreset(35, 2, 4, 12));
            strong.addActionListener(e -> applyPreset(55, 3, 6, 20));
            reroll.addActionListener(e -> {
                seed.setValue(System.nanoTime());
                enabled.setSelected(true);
                updateModel();
            });
            clear.addActionListener(e -> {
                state.aging.setEnabled(false)
                        .setIntensity(35)
                        .setGrainSize(2)
                        .setDamageSize(4)
                        .setScratchStrength(10)
                        .setOffsetX(0)
                        .setOffsetY(0)
                        .setSeed(43821L);
                load();
                scheduleRender();
            });
        }

        private void applyPreset(int i, int g, int d, int s) {
            loading = true;
            enabled.setSelected(true);
            intensity.setValue(i);
            grain.setValue(g);
            damage.setValue(d);
            scratches.setValue(s);
            loading = false;
            updateModel();
        }

        private void updateModel() {
            if (loading) return;
            state.aging.setEnabled(enabled.isSelected())
                    .setIntensity(intensity.intValue())
                    .setGrainSize(grain.intValue())
                    .setDamageSize(damage.intValue())
                    .setScratchStrength(scratches.intValue())
                    .setOffsetX(position.getXOffset())
                    .setOffsetY(position.getYOffset())
                    .setSeed(((Number) seed.getValue()).longValue());
            scheduleRender();
        }

        void load() {
            loading = true;
            enabled.setSelected(state.aging.isEnabled());
            intensity.setValue(state.aging.getIntensity());
            grain.setValue(state.aging.getGrainSize());
            damage.setValue(state.aging.getDamageSize());
            scratches.setValue(state.aging.getScratchStrength());
            seed.setValue(state.aging.getSeed());
            position.setPosition(state.aging.getOffsetX(), state.aging.getOffsetY());
            loading = false;
        }
    }

    private static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.insets = new Insets(8, 4, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private static void addHeading(JPanel panel, GridBagConstraints gbc, String title, String description) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(title);
        h.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 20));
        h.setForeground(SealStudioFrame.TEXT);
        JLabel p = new JLabel(description);
        p.setForeground(SealStudioFrame.MUTED);
        box.add(h);
        box.add(Box.createVerticalStrut(5));
        box.add(p);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(box, gbc);
        gbc.gridwidth = 1;
    }

    private static void addSubheading(JPanel panel, GridBagConstraints gbc, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        label.setForeground(SealStudioFrame.TEXT);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 4, 8, 12);
        panel.add(label, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 4, 8, 12);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent component) {
        JLabel name = new JLabel(label);
        name.setForeground(SealStudioFrame.MUTED);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(name, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private static JPanel transparentFlow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    private static DocumentListener documentListener(Runnable action) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        };
    }
}
