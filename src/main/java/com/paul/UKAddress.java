package com.paul;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 11:54:18 AM on Jun 27, 2006
 */
public class UKAddress extends Address {

   public String getVersion() {
      return "1.1";
   }

   public double[] printVersion(int[] i) {
      System.out.println(getVersion() + " ,Parameter " + i[0]);
      double[] d = new double[2];
      d[0] = Double.valueOf(getVersion()).doubleValue();
      d[1] = 29.9;
      return d;
   }
}
