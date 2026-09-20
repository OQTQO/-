package cn.localhost01.seal.ui;

import cn.localhost01.seal.SealUtil;
import cn.localhost01.seal.configuration.SealConfiguration;
import cn.localhost01.seal.configuration.SealFont;
import cn.localhost01.seal.ui.components.NumberStepper;
import cn.localhost01.seal.ui.components.PositionControl;
import cn.localhost01.seal.ui.model.SealEditorState;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

final class PublicSealPanel extends JPanel {
    private final SealEditorState state = new SealEditorState();
    private final SealPreviewPanel preview = new SealPreviewPanel();
    private final JTabbedPane editorTabs = new JTabbedPane();
    private final JLabel status = new JLabel("准备就绪");
    private final Timer renderTimer = new Timer(110, e -> renderPreview());
    private final AtomicInteger renderGeneration = new AtomicInteger();

    private final TextEditor mainEditor = new TextEditor("主文字", state.main, true);
    private final TextEditor centerEditor = new TextEditor("中心内容", state.center, false);
    private final TextEditor viceEditor = new TextEditor("底部文字", state.vice, true);
    private final TextEditor titleEditor = new TextEditor("抬头文字", state.title, false);
    private final CircleEditor borderEditor = new CircleEditor("外边线", state.border, false);
    private final CircleEditor borderInnerEditor = new CircleEditor("内边线", state.borderInner, true);
    private final CircleEditor innerEditor = new CircleEditor("内环", state.inner, true);
    private final AgingEditor agingEditor = new AgingEditor();
    private final BasicEditor basicEditor = new BasicEditor();

    private BufferedImage currentImage;

    PublicSealPanel() {
        super(new BorderLayout(14, 0));
        setBackground(SealStudioFrame.BACKGROUND);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        renderTimer.setRepeats(false);

        editorTabs.setBorder(null);
        editorTabs.addTab("基础", wrap(basicEditor));
        editorTabs.addTab("老化", wrap(agingEditor));
        editorTabs.addTab("主文字", wrap(mainEditor));
        editorTabs.addTab("中心内容", wrap(centerEditor));
        editorTabs.addTab("底部文字", wrap(viceEditor));
        editorTabs.addTab("抬头文字", wrap(titleEditor));
        editorTabs.addTab("外边线", wrap(borderEditor));
        editorTabs.addTab("内边线", wrap(borderInnerEditor));
        editorTabs.addTab("内环", wrap(innerEditor));
        editorTabs.addChangeListener(e -> syncVisibleEditor());

        JPanel editorCard = SealStudioFrame.card(new BorderLayout());
        editorCard.add(editorTabs, BorderLayout.CENTER);

        JPanel previewCard = buildPreviewCard();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorCard, previewCard);
        split.setBorder(null);
        split.setResizeWeight(0.66);
        split.setDividerLocation(770);
        split.setContinuousLayout(true);

        add(split, BorderLayout.CENTER);

        preview.setDragListener((dx, dy) -> {
            TextEditor target = activeTextEditor();
            if (target != null) {
                target.nudge(dx, dy);
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

        JLabel dragHint = new JLabel("切到文字页后，可在预览中拖动当前文字");
        dragHint.setForeground(SealStudioFrame.MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
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
        switch (editorTabs.getSelectedIndex()) {
            case 0 -> basicEditor.load();
            case 1 -> agingEditor.load();
            case 2 -> mainEditor.load();
            case 3 -> centerEditor.load();
            case 4 -> viceEditor.load();
            case 5 -> titleEditor.load();
            case 6 -> borderEditor.load();
            case 7 -> borderInnerEditor.load();
            case 8 -> innerEditor.load();
            default -> {
            }
        }
    }

    private TextEditor activeTextEditor() {
        return switch (editorTabs.getSelectedIndex()) {
            case 2 -> mainEditor;
            case 3 -> centerEditor;
            case 4 -> viceEditor;
            case 5 -> titleEditor;
            default -> null;
        };
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

    private void bindMove(String keystroke, int dx, int dy) {
        String key = "move-" + keystroke;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keystroke), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                TextEditor editor = activeTextEditor();
                if (editor != null) {
                    editor.nudge(dx, dy);
                }
            }
        });
    }

    private void loadAll() {
        basicEditor.load();
        agingEditor.load();
        mainEditor.load();
        centerEditor.load();
        viceEditor.load();
        titleEditor.load();
        borderEditor.load();
        borderInnerEditor.load();
        innerEditor.load();
    }

    private void scheduleRender() {
        renderTimer.restart();
    }

    private void renderPreview() {
        final int generation = renderGeneration.incrementAndGet();
        final SealConfiguration configuration = state.toConfiguration();
        final long started = System.nanoTime();

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return SealUtil.buildSeal(configuration);
            }

            @Override
            protected void done() {
                if (generation != renderGeneration.get()) {
                    return;
                }
                try {
                    currentImage = get();
                    preview.setImage(currentImage);
                    long millis = (System.nanoTime() - started) / 1_000_000;
                    status.setForeground(SealStudioFrame.MUTED);
                    status.setText(currentImage.getWidth() + " x " + currentImage.getHeight()
                            + " px · Graphics2D · " + millis + " ms"
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

    private void shapeChanged(boolean round) {
        if (round) {
            state.border.height = state.border.width;
            state.borderInner.height = state.borderInner.width;
            state.inner.height = state.inner.width;
        } else {
            state.border.height = Math.max(80, (int) Math.round(state.border.width * 0.72));
            state.borderInner.height = Math.max(70, (int) Math.round(state.borderInner.width * 0.71));
            state.inner.height = Math.max(50, (int) Math.round(state.inner.width * 0.62));
        }
        borderEditor.load();
        borderInnerEditor.load();
        innerEditor.load();
        scheduleRender();
    }

    private final class BasicEditor extends JPanel {
        private final JRadioButton round = new JRadioButton("圆形");
        private final JRadioButton oval = new JRadioButton("椭圆", true);
        private final JTextField mainText = new JTextField();
        private final JTextField centerText = new JTextField();
        private final JTextField viceText = new JTextField();
        private final JTextField titleText = new JTextField();
        private final NumberStepper size = new NumberStepper(340, 160, 1600, 10);
        private final JButton color = new JButton("选择颜色");
        private boolean loading;

        BasicEditor() {
            super(new GridBagLayout());
            setOpaque(false);
            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, "基础参数", "先确定内容和章形，再到各分类页精调样式与位置。");

            ButtonGroup shapes = new ButtonGroup();
            shapes.add(round);
            shapes.add(oval);
            JPanel shapeBox = transparentFlow();
            round.setOpaque(false);
            oval.setOpaque(false);
            shapeBox.add(round);
            shapeBox.add(oval);
            addRow(this, gbc, "印章形状", shapeBox);

            addRow(this, gbc, "主文字", mainText);
            addRow(this, gbc, "中心内容", centerText);
            addRow(this, gbc, "底部文字", viceText);
            addRow(this, gbc, "抬头文字", titleText);

            color.setOpaque(true);
            color.setForeground(Color.WHITE);
            color.addActionListener(e -> {
                Color selected = JColorChooser.showDialog(PublicSealPanel.this, "选择印章颜色", state.color);
                if (selected != null) {
                    state.color = selected;
                    color.setBackground(selected);
                    scheduleRender();
                }
            });
            addRow(this, gbc, "印章颜色", color);
            addRow(this, gbc, "画布尺寸", size);

            JPanel presets = transparentFlow();
            JButton roundPreset = new JButton("标准圆章");
            JButton ovalPreset = new JButton("标准椭圆章");
            JButton upstream = new JButton("原项目示例");
            presets.add(roundPreset);
            presets.add(ovalPreset);
            presets.add(upstream);
            addRow(this, gbc, "快速模板", presets);

            round.addActionListener(e -> {
                if (!loading) shapeChanged(true);
            });
            oval.addActionListener(e -> {
                if (!loading) shapeChanged(false);
            });
            size.addChangeListener(e -> {
                if (!loading) {
                    state.imageSize = size.intValue();
                    scheduleRender();
                }
            });

            bindText(mainText, value -> state.main.text = value);
            bindText(centerText, value -> state.center.text = value);
            bindText(viceText, value -> state.vice.text = value);
            bindText(titleText, value -> state.title.text = value);

            roundPreset.addActionListener(e -> {
                state.applyRoundPreset();
                loadAll();
                scheduleRender();
            });
            ovalPreset.addActionListener(e -> {
                state.applyOvalPreset();
                loadAll();
                scheduleRender();
            });
            upstream.addActionListener(e -> {
                state.applyUpstreamExample();
                loadAll();
                scheduleRender();
            });
        }

        void load() {
            loading = true;
            boolean isRound = state.border.width == state.border.height;
            round.setSelected(isRound);
            oval.setSelected(!isRound);
            mainText.setText(state.main.text);
            centerText.setText(state.center.text);
            viceText.setText(state.vice.text);
            titleText.setText(state.title.text);
            size.setValue(state.imageSize);
            color.setBackground(state.color);
            loading = false;
        }

        private void bindText(JTextField field, java.util.function.Consumer<String> consumer) {
            field.getDocument().addDocumentListener(documentListener(() -> {
                if (!loading) {
                    consumer.accept(field.getText());
                    scheduleRender();
                }
            }));
        }
    }

    private final class TextEditor extends JPanel {
        private final String name;
        private final SealEditorState.TextState model;
        private final boolean arc;
        private final JCheckBox enabled = new JCheckBox("启用");
        private final JTextField text = new JTextField();
        private final JComboBox<String> family = new JComboBox<>(SealFont.getSupportFontNames());
        private final NumberStepper size = new NumberStepper(24, 8, 220, 1);
        private final NumberStepper space = new NumberStepper(12.0, 0.0, 180.0, 0.5);
        private final NumberStepper margin = new NumberStepper(0, -300, 300, 1);
        private final JCheckBox bold = new JCheckBox("加粗", true);
        private final PositionControl position = new PositionControl();
        private boolean loading;

        TextEditor(String name, SealEditorState.TextState model, boolean arc) {
            super(new GridBagLayout());
            this.name = name;
            this.model = model;
            this.arc = arc;
            setOpaque(false);

            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, name, arc
                    ? "沿原 SealUtil 圆弧/椭圆算法排版，XY 只做最终整体平移。"
                    : "普通文字保持原排版方式，XY 用于最终位置精调。");

            enabled.setOpaque(false);
            bold.setOpaque(false);
            JPanel flags = transparentFlow();
            flags.add(enabled);
            flags.add(bold);
            addRow(this, gbc, "状态", flags);
            addRow(this, gbc, "文字", text);
            addRow(this, gbc, "字体", family);
            addRow(this, gbc, "字号", size);
            if (arc) {
                addRow(this, gbc, "文字展开角度", space);
            }
            addRow(this, gbc, arc ? "距边框距离" : "垂直基准偏移", margin);

            addSubheading(this, gbc, "位置微调");
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(position, gbc);
            gbc.gridwidth = 1;

            enabled.addActionListener(e -> updateModel());
            bold.addActionListener(e -> updateModel());
            family.addActionListener(e -> updateModel());
            size.addChangeListener(e -> updateModel());
            space.addChangeListener(e -> updateModel());
            margin.addChangeListener(e -> updateModel());
            text.getDocument().addDocumentListener(documentListener(this::updateModel));
            position.setPositionListener((x, y) -> {
                if (!loading) {
                    model.offsetX = x;
                    model.offsetY = y;
                    scheduleRender();
                }
            });
        }

        void updateModel() {
            if (loading) return;
            model.enabled = enabled.isSelected();
            model.text = text.getText();
            model.family = String.valueOf(family.getSelectedItem());
            model.size = size.intValue();
            model.space = space.doubleValue();
            model.margin = margin.intValue();
            model.bold = bold.isSelected();
            scheduleRender();
        }

        void load() {
            loading = true;
            enabled.setSelected(model.enabled);
            text.setText(model.text == null ? "" : model.text);
            family.setSelectedItem(model.family);
            size.setValue(model.size);
            space.setValue(model.space);
            margin.setValue(model.margin);
            bold.setSelected(model.bold);
            position.setPosition(model.offsetX, model.offsetY);
            loading = false;
        }

        void nudge(int dx, int dy) {
            int nextX = Math.max(-500, Math.min(500, model.offsetX + dx));
            int nextY = Math.max(-500, Math.min(500, model.offsetY + dy));
            position.setPosition(nextX, nextY);
        }
    }

    private final class CircleEditor extends JPanel {
        private final SealEditorState.CircleState model;
        private final boolean optional;
        private final JCheckBox enabled = new JCheckBox("启用");
        private final NumberStepper line = new NumberStepper(2, 1, 40, 1);
        private final NumberStepper width = new NumberStepper(280, 20, 1500, 2);
        private final NumberStepper height = new NumberStepper(200, 20, 1500, 2);
        private final JCheckBox lock = new JCheckBox("锁定宽高比例");
        private boolean loading;
        private double lockedRatio = 1.0;

        CircleEditor(String name, SealEditorState.CircleState model, boolean optional) {
            super(new GridBagLayout());
            this.model = model;
            this.optional = optional;
            setOpaque(false);

            GridBagConstraints gbc = baseGbc();
            addHeading(this, gbc, name, "界面显示完整宽高；内部仍转换为原 SealCircle 的横纵半径。");
            enabled.setOpaque(false);
            lock.setOpaque(false);
            if (optional) addRow(this, gbc, "状态", enabled);
            addRow(this, gbc, "线条宽度", line);
            addRow(this, gbc, "宽度", width);
            addRow(this, gbc, "高度", height);
            addRow(this, gbc, "比例", lock);

            enabled.addActionListener(e -> updateModel(false));
            line.addChangeListener(e -> updateModel(false));
            width.addChangeListener(e -> updateModel(true));
            height.addChangeListener(e -> updateModel(false));
            lock.addActionListener(e -> {
                if (lock.isSelected()) {
                    lockedRatio = Math.max(0.01, width.doubleValue() / Math.max(1.0, height.doubleValue()));
                }
            });
        }

        private void updateModel(boolean fromWidth) {
            if (loading) return;
            if (lock.isSelected() && fromWidth) {
                loading = true;
                int linkedHeight = Math.max(20, (int) Math.round(width.doubleValue() / lockedRatio));
                height.setValue(Math.min(1500, linkedHeight));
                loading = false;
            }
            model.enabled = !optional || enabled.isSelected();
            model.lineSize = line.intValue();
            model.width = width.intValue();
            model.height = height.intValue();
            scheduleRender();
        }

        void load() {
            loading = true;
            enabled.setSelected(model.enabled);
            line.setValue(model.lineSize);
            width.setValue(model.width);
            height.setValue(model.height);
            lockedRatio = Math.max(0.01, model.width / (double) Math.max(1, model.height));
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
            addHeading(this, gbc, "印泥老化", "只处理已有印泥像素的透明度，不向透明背景添加杂点；同一纹理种子可重复得到相同结果。");

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
            gbc.gridwidth = 1;

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
                state.resetAging();
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
            state.aging
                    .setEnabled(enabled.isSelected())
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
