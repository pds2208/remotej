package evaluation.bank;

import java.rmi.RemoteException;
import java.rmi.Remote;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 11:53:18 AM
 */
public interface IBank extends Remote {
   public void debit(double amount) throws RemoteException;
   public void credit(IAmount amount) throws RemoteException;
   public void create() throws RemoteException;
   public void open() throws RemoteException;
   public void close() throws RemoteException;
   public double getBalance() throws RemoteException;
}
