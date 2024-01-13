package evaluation.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Amount implements IAmount {

    private double number;
    private boolean exported = false;

    public Amount() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
        exported = true;
    }

    public Amount(double number) throws RemoteException {
        this.number = number;
        UnicastRemoteObject.exportObject(this, 0);
        exported = true;
    }

    public double getNumber() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
        exported = true;
        return number;
    }

    public void setNumber(double number) throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
        exported = true;
        System.err.println("Setting amount");
        this.number = number;
    }
}
