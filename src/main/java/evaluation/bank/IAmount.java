package evaluation.bank;

import java.rmi.RemoteException;

public interface IAmount extends java.rmi.Remote  {

   public abstract double getNumber() throws RemoteException;

   public abstract void setNumber(double number) throws RemoteException ;

}