/*
 * @(#)DrawerPanel.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package org.remotej.ddl.drawer;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
final class DrawerPanel extends JPanel {
    private final Drawer drawer;

    public DrawerPanel(Drawer drawer) {
        setPreferredSize(new Dimension(4096, 4096));
        this.drawer = drawer;
    }

    public final void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawer.paintAST(g);
    }
}
