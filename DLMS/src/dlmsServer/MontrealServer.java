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
import dlmsServerInterfaceImp.MonLibraryImplementation;

public class MontrealServer {
	public static void receive(MonLibraryImplementation implementation) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(Constants.MONTREAL_SERVER_PORT);
			byte[] buffer = new byte[1000];
			System.out.println("Server Montreal Started............");
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
			MonLibraryImplementation monStub = new MonLibraryImplementation();
			Runnable task = () -> {
				receive(monStub);
			};

			Thread thread = new Thread(task);
			thread.start();

			Registry registry = LocateRegistry.createRegistry(Constants.MONTREAL_SERVER_PORT);
			registry.bind(Constants.MONTREAL_SERVER_NAME,monStub);
			registry.bind("addItem", monStub);
			registry.bind("removeItem", monStub);
			registry.bind("listItemAvailability", monStub);
			registry.bind("findItem", monStub);
			registry.bind("returnItem", monStub);
			registry.bind("borrowItem", monStub);	

			System.out.println("Montreal server is started...");
		} catch (RemoteException e) {
			e.printStackTrace();	
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
}
