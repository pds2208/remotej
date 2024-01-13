/*
 * @(#)DrawingTree.java                        2.1 2003/10/07
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

import java.awt.*;

final class DrawingTree {

   private final String caption;
   final int width;
   final int height;
   private final Point pos;
   final Point offset;
   Polygon contour;
   private DrawingTree parent;
   DrawingTree[] children;

   public DrawingTree(String caption, int w, int h) {
      this.caption = caption;
      this.width = w;
      this.height = h;
      this.parent = null;
      this.children = null;
      this.pos = new Point(0, 0);
      this.offset = new Point(0, 0);
      this.contour = new Polygon();
   }

   public final void setChildren(DrawingTree[] children) {
      this.children = children;
      for (DrawingTree aChildren : children)
         aChildren.parent = this;
   }

   private final int FIXED_FONT_HEIGHT = 10;
   private final Color nodeColor = new Color(250, 220, 100);

   public final void paint(Graphics graphics) {
      graphics.setColor(nodeColor);
      graphics.fillRect(pos.x, pos.y, width, height);
      graphics.setColor(Color.black);
      graphics.drawRect(pos.x, pos.y, width - 1, height - 1);

      graphics.drawString(caption, pos.x + 2,
         pos.y + (height + FIXED_FONT_HEIGHT) / 2);

      if (children != null) {
         for (DrawingTree aChildren : children) {
            aChildren.paint(graphics);
         }
      }

      if (parent != null) {
         graphics.drawLine(pos.x + width / 2, pos.y,
            parent.pos.x + parent.width / 2,
            parent.pos.y + parent.height);
      }
   }

   public final void position(Point pos) {

      this.pos.x = pos.x + this.offset.x;
      this.pos.y = pos.y + this.offset.y;

      Point temp = new Point(this.pos.x, this.pos.y);

      if (children != null) {
         for (DrawingTree aChildren : children) {
            aChildren.position(temp);
            temp.x += aChildren.offset.x;
            temp.y = this.pos.y + children[0].offset.y;
         }
      }
   }

}