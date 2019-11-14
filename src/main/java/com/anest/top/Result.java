package com.anest.top;

/*
 * Decompiled with CFR 0_118.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Result extends Frame {

    public final int width = 400;
    public final int height = 45;
    public final int center = 200;

    private JLabel lblA;
    private JLabel lblQ;
    private JLabel lblQuery;

    Font font = new Font("Arial", Font.PLAIN , 11);

    public Result() {
        this.initComponents();
    }

    public JLabel getLblA() {
        return this.lblA;
    }

    public JLabel getLblQ() {
        return this.lblQ;
    }

    public JLabel getLblQuery() {
        return this.lblQuery;
    }

    private void initComponents() {
        this.lblQuery = new JLabel();
        this.lblA = new JLabel();
        this.lblQ = new JLabel();
        this.lblA.setFont(font);
        this.lblQ.setFont(font);
        this.lblQuery.setFont(font);
        this.setAlwaysOnTop(true);
        this.setBackground(new Color(255, 0, 0));
        this.setBounds(new Rectangle((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - center, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height, width, height));
        this.setExtendedState(0);
        this.setLocation(new Point((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - center, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height));
        this.setMaximizedBounds(new Rectangle((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - center, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height, width, height));
        this.setMaximumSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(width, height));
        this.setUndecorated(true);
        this.setOpacity(0.3f);
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(new Dimension(width, height));
        this.setState(0);
        this.setType(Window.Type.UTILITY);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                Result.this.exitForm(evt);
            }
        });
        this.lblQuery.setPreferredSize(new Dimension(width, height/3));
        this.add(this.lblQuery, "North");
        this.lblA.setPreferredSize(new Dimension(width, height/3));
        this.add(this.lblA, "South");
        this.lblQ.setPreferredSize(new Dimension(width, height/3));
        this.add(this.lblQ, "Center");
        this.pack();
    }

    private void exitForm(WindowEvent evt) {
        System.exit(0);
    }

}
