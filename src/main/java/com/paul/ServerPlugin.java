package com.paul;

/**
 * ServerPlugin
 * <p/>
 * Version 1.0
 * <p/>
 * Copyright Data Systems & Solutions (2007)
 * This source is the property of Data Systems & Solutions Ltd and
 * must not be used for any purpose by any party unless specific
 * written consent has been given by an authorised signatory of Data
 * Systems and Solutions Ltd.
 */
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
