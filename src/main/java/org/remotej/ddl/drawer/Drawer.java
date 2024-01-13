/*
 * @(#)Drawer.java                        2.1 2003/10/07
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

import org.remotej.ddl.trees.Service;

import java.awt.*;

public final class Drawer {

    private DrawerPanel panel;
    private DrawingTree theDrawing;

    // Draw the AST representing a complete program.

    public final void draw(Service ast) {
        panel = new DrawerPanel(this);
        DrawerFrame frame = new DrawerFrame(panel);

        Font font = new Font("SansSerif", Font.PLAIN, 12);
        frame.setFont(font);

        // Frame must be visible otherwise the graphics won't be created
        frame.setVisible(true);

        LayoutVisitor layout = new LayoutVisitor(panel.getGraphics());
        theDrawing = (DrawingTree) ast.visit(layout, null);
        theDrawing.position(new Point(2048, 10));

    }

    public final void paintAST(Graphics g) {
        g.setColor(panel.getBackground());
        Dimension d = panel.getSize();
        g.fillRect(0, 0, d.width, d.height);

        if (theDrawing != null) {
            theDrawing.paint(g);
        }
    }
}
