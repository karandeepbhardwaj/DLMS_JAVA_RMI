package dlmsServerInterface;
import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;

public interface AddInterface extends Remote{
	
	public String addItem(String managerID, String itemID, String itemName, int quantity) throws RemoteException;
	public String removeItem(String managerID, String itemID, int quantity, int option) throws RemoteException;
	public ArrayList<String> listItemAvailability(String managerID) throws RemoteException;
	
	public String borrowItem(String userID, String itemID) throws RemoteException, IOException;
	public String findItem(String userID, String itemName) throws RemoteException;
	public String returnItem(String userID, String itemID) throws RemoteException, IOException;
	public String waitingList(String userID, String itemID) throws RemoteException;
		
}