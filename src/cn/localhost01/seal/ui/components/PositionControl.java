package cn.localhost01.seal.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public final class PositionControl extends JPanel {
    private final JSlider xSlider = new JSlider(-150, 150, 0);
    private final JSlider ySlider = new JSlider(-150, 150, 0);
    private final NumberStepper xValue = new NumberStepper(0, -500, 500, 1);
    private final NumberStepper yValue = new NumberStepper(0, -500, 500, 1);
    private BiConsumer<Integer, Integer> listener;
    private boolean syncing;

    public PositionControl() {
        super(new GridBagLayout());
        setOpaque(false);
        xSlider.setOpaque(false);
        ySlider.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 8);
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("水平 X"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(xSlider, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(xValue, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("垂直 Y"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(ySlider, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(yValue, gbc);

        JButton reset = new JButton("位置归零");
        reset.setFocusable(false);
        gbc.gridy++;
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(reset, gbc);

        JLabel hint = new JLabel("X 正值向右 · Y 正值向下");
        hint.setForeground(new Color(120, 126, 138));
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(hint, gbc);

        xSlider.addChangeListener(e -> {
            if (syncing) return;
            syncing = true;
            xValue.setValue(xSlider.getValue());
            syncing = false;
            fire();
        });
        ySlider.addChangeListener(e -> {
            if (syncing) return;
            syncing = true;
            yValue.setValue(ySlider.getValue());
            syncing = false;
            fire();
        });
        xValue.addChangeListener(e -> {
            if (syncing) return;
            syncing = true;
            int value = xValue.intValue();
            xSlider.setValue(Math.max(xSlider.getMinimum(), Math.min(xSlider.getMaximum(), value)));
            syncing = false;
            fire();
        });
        yValue.addChangeListener(e -> {
            if (syncing) return;
            syncing = true;
            int value = yValue.intValue();
            ySlider.setValue(Math.max(ySlider.getMinimum(), Math.min(ySlider.getMaximum(), value)));
            syncing = false;
            fire();
        });
        reset.addActionListener(e -> setPosition(0, 0));
    }

    public void setPosition(int x, int y) {
        syncing = true;
        xValue.setValue(x);
        yValue.setValue(y);
        xSlider.setValue(Math.max(xSlider.getMinimum(), Math.min(xSlider.getMaximum(), x)));
        ySlider.setValue(Math.max(ySlider.getMinimum(), Math.min(ySlider.getMaximum(), y)));
        syncing = false;
        fire();
    }

    public int getXOffset() {
        return xValue.intValue();
    }

    public int getYOffset() {
        return yValue.intValue();
    }

    public void setPositionListener(BiConsumer<Integer, Integer> listener) {
        this.listener = listener;
    }

    private void fire() {
        if (listener != null) {
            listener.accept(getXOffset(), getYOffset());
        }
    }
}
