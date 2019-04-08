package dlmsServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ConstantValue.Constants;
import dlmsServerInterfaceImp.ConLibraryImplementation;

public class ConcordiaServer {

	public static void receive(ConLibraryImplementation implementation) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(Constants.CONCORDIA_SERVER_PORT);
			byte[] buffer = new byte[1000];
			System.out.println("Concordia Server Started");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String message = new String(request.getData());
				String [] data = message.split("-");
				String userID = data[0];
				String itemID = data[1];
				String itemName = data[3];
				String responseString = "";
				switch(data[2]) {
				case "1": responseString = implementation.borrowItem(userID, itemID);
					break;
				case "2": responseString = implementation.findItem(userID, itemName);
					break;
				case "3": responseString = implementation.returnItem(userID, itemID);
					break;
				}
				DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(),
						request.getPort());
				aSocket.send(reply);
			}
		}catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	public static void main (String[] args ) {
		try {
			ConLibraryImplementation conStub = new ConLibraryImplementation();
			Runnable task = () -> {
				receive(conStub);
			};
			Thread thread = new Thread(task);
			thread.start();
			
			Registry registry = LocateRegistry.createRegistry(Constants.CONCORDIA_SERVER_PORT);
			registry.bind(Constants.CONCORDIA_SERVER_NAME,conStub);
			registry.bind("addItem", conStub);
			registry.bind("removeItem", conStub);
			registry.bind("listItemAvailability", conStub);
			registry.bind("findItem", conStub);
			registry.bind("returnItem", conStub);
			registry.bind("borrowItem", conStub);

			System.out.println("Concordia server is started...");
		} catch (RemoteException e) {
			e.printStackTrace();	
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
}