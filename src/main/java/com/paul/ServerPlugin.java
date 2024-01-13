package com.paul;

public class ServerPlugin extends Thread {

   public ServerPlugin() {
   }

   public void run() {
      while (true) {
         System.err.println("ServerPlugin here");
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
      }

   }
}
