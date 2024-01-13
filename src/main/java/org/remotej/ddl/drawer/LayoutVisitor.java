/*
 * @(#)LayoutVisitor.java                        2.1 2003/10/07
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

import org.remotej.ddl.trees.*;
import org.remotej.ddl.trees.ClassValue;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

public final class LayoutVisitor implements Visitor {

   private final int BORDER = 5;
   private final int PARENT_SEP = 30;

   private final Graphics graphics;

   public LayoutVisitor(Graphics graphics) {
      this.graphics = graphics;
   }

   // Literals, Identifiers and Operators

   public final Object visitIdentifier(Identifier ast, Object obj) {
      return layout(ast.spelling);
   }

   public final Object visitIntegerLiteral(IntegerLiteral ast, Object obj) {
      return layout(ast.spelling);
   }

   public final Object visitParameterDeclaration(ParameterDeclaration ast, Object o) {
      String s = "";
      LinkedList<Parameter> list = ast.parameters;
      for (Iterator<Parameter> i = list.listIterator(); i.hasNext();) {
         Parameter p = (Parameter) i.next();
         Identifier name = p.type;
         if (s.length() > 0) {
            s += ", ";
         }
         if (name != null) {
            s += name.spelling + " ";
         }
      }
      return layout("Parameter Declaration", new Identifier(s, null));
   }

   public Object visitNameValueOption(NameValueOption ast, Object o) {
       return layout("Protocol option", ast.name, ast.value);
   }

   public final Object visitSequentialClassImportStatement(SequentialClassImportStatement ast, Object o) {
      return layout("Sequential Class Import", ast.st1, ast.st2);
   }

   public final Object visitClassImportStatement(ClassImportStatement ast, Object o) {
      return layout("Class Import", ast.importStatement);
   }

   public final Object visitMethod(Methods ast, Object o) {
     return layout("Method", ast.returnValue, ast.methodName, ast.parameters, ast.option);
   }

   public final Object visitMethodName(MethodName ast, Object o) {
      return layout("Method name", ast.methodValue);
   }

   public final Object visitSequentialMethods(SequentialMethods ast, Object o) {
      return layout("Methods", ast.opt1, ast.opt2);
   }

   public final Object visitPointcutMethods(PointcutMethods ast, Object o) {
      return layout("Pointcut methods", ast.methods);
   }

   public final Object visitImportMethods(ImportMethods ast, Object o) {
      return layout("Import methods", ast.methods);
   }

   public final Object visitReturnValue(ReturnValue ast, Object o) {
      return layout("Return Value", ast.returnValue);
   }

   public final Object visitClassValue(ClassValue ast, Object o) {
      return layout("Class Name", ast.returnValue, ast.className, ast.methodName, ast.parameters);
   }

   public final Object visitService(Service ast, Object obj) {
      return layout("ServiceDescription", ast.imports, ast.service);
   }

   public final Object visitSequentialOption(SequentialOption ast, Object o) {
      return layout("Option", ast.opt1, ast.opt2);
   }

   //public Object visitSequentialMethodOption(SequentialMethodOption ast, Object o) {
   //   return layout("Method Options", ast.opt1, ast.opt2);
   //}

   public final Object visitSequentialStatement(SequentialStatement ast, Object o) {
      return layout("Statement", ast.smt1, ast.smt2);
   }

   public final Object visitImportDeclaration(ImportDeclaration ast, Object o) {
      return layout("Import declaration", ast.aClass);
   }

   public final Object visitPointcutDeclaration(PointcutDeclaration ast, Object o) {
      if (ast.recovery != null) {
         return layout("Pointcut declaration", ast.aClass, ast.recovery);
      }
      return layout("Pointcut declaration", ast.aClass);
   }

   public final Object visitSequentialImportStatement(SequentialImportStatement ast, Object o) {
      return layout("Sequential Import", ast.st1, ast.st2);
   }

   public final Object visitSequentialPointcutStatement(SequentialPointcutStatement ast, Object o) {
      return layout("Sequential Pointcut", ast.st1, ast.st2);
   }

   public final Object visitImportOptions(ImportOptions ast, Object o) {
      return layout("Import ProtocolOptions", ast.options);
   }

   public final Object visitPointcutOptions(PointcutOptions ast, Object o) {
      return layout("Pointcut ProtocolOptions", ast.options);
   }

   public final Object visitRecoveryOption(RecoveryOption ast, Object o) {
      return layout("Recovery Routine", ast.recoveryName);
   }

   public final Object visitBooleanValue(BooleanValue ast, Object o) {
      return layout(ast.spelling);
   }

   public Object visitProtocolStatement(ProtocolStatement ast, Object o) {
      return layout("Protocol : " + ast.protocol);
   }

   public final Object visitRecoveryStatement(RecoveryStatement ast, Object o) {
      return layout("Recovery", ast.recoveryName, ast.parameters, ast.recoveryCode);
   }

   private DrawingTree layoutCaption(String name) {
      if (name.length() > 50) {
         name = name.substring(0, 46) + " ...";
      }
      int w = (int) graphics.getFontMetrics().getStringBounds(name, graphics).getWidth() + 4;
      int h = graphics.getFontMetrics().getHeight() + 4;
      return new DrawingTree(name, w, h);
   }

   private DrawingTree layout(String name, AST... child) {

      DrawingTree dt = layoutCaption(name);
      if (child.length == 0) {
         dt.contour.upper_tail = new Polyline(0, dt.height + 2 * BORDER, null);
         dt.contour.upper_head = dt.contour.upper_tail;
         dt.contour.lower_tail = new Polyline(-dt.width - 2 * BORDER, 0, null);
         dt.contour.lower_head = new Polyline(0, dt.height + 2 * BORDER, dt.contour.lower_tail);
         return dt;
      }
      DrawingTree dt1[] = new DrawingTree[child.length];
      for (int i = 0; i < child.length; i++) {
         dt1[i] = (DrawingTree) child[i].visit(this, null);
      }
      dt.setChildren(dt1);
      attachParent(dt, join(dt));
      return dt;
   }

   private void attachParent(DrawingTree dt, int w) {
      int y = PARENT_SEP;
      int x2 = (w - dt.width) / 2 - BORDER;
      int x1 = x2 + dt.width + 2 * BORDER - w;

      dt.children[0].offset.y = y + dt.height;
      dt.children[0].offset.x = x1;
      dt.contour.upper_head = new Polyline(0, dt.height,
         new Polyline(x1, y, dt.contour.upper_head));
      dt.contour.lower_head = new Polyline(0, dt.height,
         new Polyline(x2, y, dt.contour.lower_head));
   }

   private int join(DrawingTree dt) {
      int w, sum;

      dt.contour = dt.children[0].contour;
      sum = w = dt.children[0].width + 2 * BORDER;

      for (int i = 1; i < dt.children.length; i++) {
         int d = merge(dt.contour, dt.children[i].contour);
         dt.children[i].offset.x = d + w;
         dt.children[i].offset.y = 0;
         w = dt.children[i].width + 2 * BORDER;
         sum += d + w;
      }
      return sum;
   }

   private int merge(Polygon c1, Polygon c2) {
      int x, y, total, d;
      Polyline lower, upper, b;

      x = y = total = 0;
      upper = c1.lower_head;
      lower = c2.upper_head;

      while (lower != null && upper != null) {
         d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
         x += d;
         total += d;

         if (y + lower.dy <= upper.dy) {
            x += lower.dx;
            y += lower.dy;
            lower = lower.link;
         } else {
            x -= upper.dx;
            y -= upper.dy;
            upper = upper.link;
         }
      }

      if (lower != null) {
         b = bridge(c1.upper_tail, 0, 0, lower, x, y);
         c1.upper_tail = (b.link != null) ? c2.upper_tail : b;
         c1.lower_tail = c2.lower_tail;
      } else {
         b = bridge(c2.lower_tail, x, y, upper, 0, 0);
         if (b.link == null) {
            c1.lower_tail = b;
         }
      }

      c1.lower_head = c2.lower_head;

      return total;
   }

   private int offset(int p1, int p2, int a1, int a2, int b1, int b2) {
      int d, s, t;

      if (b2 <= p2 || p2 + a2 <= 0) {
         return 0;
      }

      t = b2 * a1 - a2 * b1;
      if (t > 0) {
         if (p2 < 0) {
            s = p2 * a1;
            d = s / a2 - p1;
         } else if (p2 > 0) {
            s = p2 * b1;
            d = s / b2 - p1;
         } else {
            d = -p1;
         }
      } else if (b2 < p2 + a2) {
         s = (b2 - p2) * a1;
         d = b1 - (p1 + s / a2);
      } else if (b2 > p2 + a2) {
         s = (a2 + p2) * b1;
         d = s / b2 - (p1 + a1);
      } else {
         d = b1 - (p1 + a1);
      }

      if (d > 0) {
         return d;
      } else {
         return 0;
      }
   }

   private Polyline bridge(Polyline line1, int x1, int y1,
                           Polyline line2, int x2, int y2) {
      int dy, dx, s;
      Polyline r;

      dy = y2 + line2.dy - y1;
      if (line2.dy == 0) {
         dx = line2.dx;
      } else {
         s = dy * line2.dx;
         dx = s / line2.dy;
      }

      r = new Polyline(dx, dy, line2.link);
      line1.link = new Polyline(x2 + line2.dx - dx - x1, 0, r);

      return r;
   }

    public Object visitOption(Option aThis, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
