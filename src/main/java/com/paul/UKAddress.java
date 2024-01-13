package com.paul;

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
