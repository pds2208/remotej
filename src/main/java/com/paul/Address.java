package com.paul;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 2:20:20 PM on Mar 5, 2006
 */
public class Address {

   public static void main(String[] args) {
      Address a = new Address();
      while (true) {
         a.publish("This is a test String...");
         System.out.println("Received: " + a.subscribe());
      }
   }
   public final Paul getName(int s, String y) {
      return new Paul();
   }

   public String subscribe() {
     return null;
   }

   public void publish(String s) {
   }

   public void sendMessage(String s) {
      System.out.println("sendMessage: " + s);
   }
   
   public final String getAddress(String a, String b) {
      return "Elmbrook Patch Elm Lane Rangeworthy BS37 7LU";
   }

   public final void dummy(Harry h) {
      h.printName(new Paul());
   }

}
