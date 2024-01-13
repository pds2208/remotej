package com.paul;

/**
 * User: soulep
 * Date: Mar 26, 2008
 * Time: 1:34:59 PM
 */
public class Subscriber {
   public static void main(String[] args) throws InterruptedException {
      Subscriber a = new Subscriber();
      while (true) {
         a.subscribe("This is not from the producer");
         Thread.sleep(5000);
      }
   }

   public synchronized void subscribe(String message) {
     System.out.println("Received: " + message);
   }
}
