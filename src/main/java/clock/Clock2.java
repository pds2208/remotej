package clock;

/*
 * @(#)Clock2.java	1.5 98/07/09
 *
 * Copyright (c) 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;

public class Clock2 extends JFrame implements Runnable {

   Thread timer;                // The thread that displays clock
   int lastxs, lastys, lastxm;
   int lastym, lastxh, lastyh;  // Dimensions used to draw hands
   SimpleDateFormat formatter;  // Formats the date displayed
   String lastdate;             // String to hold date displayed
   Font clockFaceFont;          // Font for number display on clock
   Date currentDate;            // Used to get date to display
   Color handColor;             // Color of main hands and dial
   Color numberColor;           // Color of second hand and numbers
   ClockDate date = new ClockDate();

   public Clock2() {
      lastxs = lastys = lastxm = lastym = lastxh = lastyh = 0;
      currentDate = new Date();
      formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy", Locale.getDefault());
      lastdate = formatter.format(currentDate);
      clockFaceFont = new Font("Serif", Font.PLAIN, 14);
      handColor = Color.blue;
      numberColor = Color.darkGray;

      setSize(200, 200);              // Set clock window size
      this.start();

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            setVisible(false);
            dispose();
            System.exit(0);
         }
      });
   }

   // Plotpoints allows calculation to only cover 45 degrees of the circle,
   // and then mirror
   public void plotpoints(int x0, int y0, int x, int y, Graphics g) {
      g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
      g.drawLine(x0 + y, y0 + x, x0 + y, y0 + x);
      g.drawLine(x0 + y, y0 - x, x0 + y, y0 - x);
      g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
      g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
      g.drawLine(x0 - y, y0 - x, x0 - y, y0 - x);
      g.drawLine(x0 - y, y0 + x, x0 - y, y0 + x);
      g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
   }

   // Circle is just Bresenham's algorithm for a scan converted circle
   public void circle(int x0, int y0, int r, Graphics g) {
      int x, y;
      float d;
      x = 0;
      y = r;
      d = (float) 5 / 4 - r;
      plotpoints(x0, y0, x, y, g);

      while (y > x) {
         if (d < 0) {
            d = d + 2 * x + 3;
            x++;
         } else {
            d = d + 2 * (x - y) + 5;
            x++;
            y--;
         }
         plotpoints(x0, y0, x, y, g);
      }
   }

   public Date getDate() {
      return date.getDate();
   }
   
   // Paint is the main part of the program
   @SuppressWarnings("deprecation")
   public void paint(Graphics g) {
      int xh, yh, xm, ym, xs, ys, s, m, h, xcenter, ycenter;
      String today;

      currentDate = getDate();
      s = currentDate.getSeconds();
      m = currentDate.getMinutes();
      h = currentDate.getHours();


      SimpleDateFormat formatter = new SimpleDateFormat("s", Locale.getDefault());


      formatter.applyPattern("m");
      formatter.applyPattern("h");
      formatter.applyPattern("EEE MMM dd HH:mm:ss yyyy");
      today = formatter.format(currentDate);
      xcenter = 80;
      ycenter = 80;

      xs = (int) (Math.cos(s * 3.14f / 30 - 3.14f / 2) * 45 + xcenter);
      ys = (int) (Math.sin(s * 3.14f / 30 - 3.14f / 2) * 45 + ycenter);
      xm = (int) (Math.cos(m * 3.14f / 30 - 3.14f / 2) * 40 + xcenter);
      ym = (int) (Math.sin(m * 3.14f / 30 - 3.14f / 2) * 40 + ycenter);
      xh = (int) (Math.cos((h * 30 + m / 2) * 3.14f / 180 - 3.14f / 2) * 30 + xcenter);
      yh = (int) (Math.sin((h * 30 + m / 2) * 3.14f / 180 - 3.14f / 2) * 30 + ycenter);

      // Draw the circle and numbers
      g.setFont(clockFaceFont);
      g.setColor(handColor);
      circle(xcenter, ycenter, 50, g);
      g.setColor(numberColor);
      g.drawString("9", xcenter - 45, ycenter + 3);
      g.drawString("3", xcenter + 40, ycenter + 3);
      g.drawString("12", xcenter - 5, ycenter - 37);
      g.drawString("6", xcenter - 3, ycenter + 45);

      // Erase if necessary, and redraw
      g.setColor(getBackground());
      if (xs != lastxs || ys != lastys) {
         g.drawLine(xcenter, ycenter, lastxs, lastys);
         g.drawString(lastdate, 5, 175);
      }
      if (xm != lastxm || ym != lastym) {
         g.drawLine(xcenter, ycenter - 1, lastxm, lastym);
         g.drawLine(xcenter - 1, ycenter, lastxm, lastym);
      }
      if (xh != lastxh || yh != lastyh) {
         g.drawLine(xcenter, ycenter - 1, lastxh, lastyh);
         g.drawLine(xcenter - 1, ycenter, lastxh, lastyh);
      }
      g.setColor(numberColor);

      g.drawString("                        ", 5, 175);
      g.drawString(today, 5, 175);

      g.drawLine(xcenter, ycenter, xs, ys);
      g.setColor(handColor);
      g.drawLine(xcenter - 1, ycenter, xm, ym);
      g.drawLine(xcenter, ycenter - 1, xh, yh);
      g.drawLine(xcenter - 1, ycenter, xh, yh);
      lastxs = xs;
      lastys = ys;
      lastxm = xm;
      lastym = ym;
      lastxh = xh;
      lastyh = yh;
      lastdate = today;
      currentDate = null;
   }

   public void start() {
      timer = new Thread(this);
      timer.start();
   }

   public void stop() {
      timer = null;
   }

   public void run() {
      Thread me = Thread.currentThread();
      while (timer == me) {
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
         }
         repaint();
      }
   }


   // ----------------------------------------------------------------------------------
   // Main Method
   // ----------------------------------------------------------------------------------
   @SuppressWarnings("deprecation")
   public static void main(String[] argv) {
      Clock2 clock = new Clock2();
      clock.show();
   }

}
