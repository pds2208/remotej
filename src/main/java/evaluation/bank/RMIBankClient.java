package evaluation.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 12:01:50 PM
 */
public class RMIBankClient {
   public static void main(String[] args) {
      new RMIBankClient().doIt();
   }
   
   private void doIt() {
      IBank bank;
      try {
         bank = (IBank) Naming.lookup("//localhost/BankRMI");
         //bank = new BankRMI();
         bank.create();
         bank.open();
         Amount amt = new Amount(25000.00);
         setNumber(amt);
         
         bank.debit(5000.00);
         System.out.println("Balance is: " + bank.getBalance());
      } catch (Exception e) {
         System.out.println("RMIBankClient exception: " + e.getMessage());
         e.printStackTrace();
      }
   }
   
   private void setNumber(Amount amt) {
      try {
         IBank bank = (IBank) Naming.lookup("//localhost/BankRMI");
         amt.setNumber(25000.00);
         bank.credit(amt);
      } catch (MalformedURLException e) {
         e.printStackTrace();
      } catch (RemoteException e) {
         e.printStackTrace();
      } catch (NotBoundException e) {
         e.printStackTrace();
      }
      
   }
}
