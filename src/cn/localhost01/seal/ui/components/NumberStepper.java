package cn.localhost01.seal.ui.components;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public final class NumberStepper extends JPanel {
    private final JSpinner spinner;
    private final Number step;

    public NumberStepper(Number value, Comparable<?> min, Comparable<?> max, Number step) {
        super(new BorderLayout(6, 0));
        this.step = step;
        setOpaque(false);

        JButton minus = new JButton("−");
        JButton plus = new JButton("+");
        minus.setFocusable(false);
        plus.setFocusable(false);
        minus.setPreferredSize(new Dimension(44, 34));
        plus.setPreferredSize(new Dimension(44, 34));

        spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setPreferredSize(new Dimension(96, 34));

        minus.addActionListener(e -> increment(-1));
        plus.addActionListener(e -> increment(1));

        add(minus, BorderLayout.WEST);
        add(spinner, BorderLayout.CENTER);
        add(plus, BorderLayout.EAST);
    }

    private void increment(int direction) {
        Number current = (Number) spinner.getValue();
        if (current instanceof Double || current instanceof Float || step instanceof Double || step instanceof Float) {
            double next = current.doubleValue() + step.doubleValue() * direction;
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            Comparable<?> min = model.getMinimum();
            Comparable<?> max = model.getMaximum();
            if (min != null && next < ((Number) min).doubleValue()) return;
            if (max != null && next > ((Number) max).doubleValue()) return;
            spinner.setValue(next);
        } else {
            int next = current.intValue() + step.intValue() * direction;
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            Comparable<?> min = model.getMinimum();
            Comparable<?> max = model.getMaximum();
            if (min != null && next < ((Number) min).intValue()) return;
            if (max != null && next > ((Number) max).intValue()) return;
            spinner.setValue(next);
        }
    }

    public int intValue() {
        return ((Number) spinner.getValue()).intValue();
    }

    public double doubleValue() {
        return ((Number) spinner.getValue()).doubleValue();
    }

    public void setValue(Number value) {
        spinner.setValue(value);
    }

    public void addChangeListener(ChangeListener listener) {
        spinner.addChangeListener(listener);
    }

    public JSpinner spinner() {
        return spinner;
    }
}
