package evaluation.bank;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 11:51:14 AM
 */
@SuppressWarnings("serial")
public class BankRMI extends UnicastRemoteObject implements IBank {

   private double balance;

   public BankRMI() throws RemoteException {
   }

   public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new RMISecurityManager());
		}
		try {
		    BankRMI obj = new BankRMI();
		    Naming.rebind("//localhost/BankRMI", obj);
		    System.out.println("BankRMI bound in registry");
		} catch (Exception e) {
		    System.out.println("BankRMI err: " + e.getMessage());
		    e.printStackTrace();
		}
   }

   public void debit(double amount) throws RemoteException {
      balance -= amount;
   }

   public void credit(IAmount amount) throws RemoteException {
      balance += amount.getNumber();
   }
   
   public void create() throws RemoteException {
      balance = 0;
   }

   public void open() throws RemoteException {
      // open balance file
   }

   public void close() throws RemoteException {
      // close balance file
   }

   public double getBalance() throws RemoteException {
      return balance;
   }
}
