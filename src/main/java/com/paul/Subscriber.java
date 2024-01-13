package com.paul;

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
