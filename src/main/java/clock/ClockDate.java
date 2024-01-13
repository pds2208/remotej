package clock;

import java.util.Date;

/**
 * Copyright(c) Paul Soule 2006.
 * <p/>
 * Date: Feb 24, 2007
 * Time: 9:30:04 AM
 */

public class ClockDate {

    public Date getDate() {
      return new Date();
   }

   public static void main(String[] args) throws InterruptedException {
      ClockDate cd = new ClockDate();
      while (true) {
         System.out.println("Current Date: " + cd.getDate());
         Thread.sleep(2000);
      }
   }
}
