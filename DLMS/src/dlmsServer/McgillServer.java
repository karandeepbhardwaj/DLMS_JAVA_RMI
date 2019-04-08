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
import dlmsServerInterfaceImp.McGillLibraryImplementation;

public class McgillServer {
	public static void receive(McGillLibraryImplementation implementation) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(Constants.MCGILL_SERVER_PORT);
			byte[] buffer = new byte[1000];
			System.out.println("Server McGill Started............");
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
			McGillLibraryImplementation mcgillStub = new McGillLibraryImplementation();
			Runnable task = () -> {
				receive(mcgillStub);
			};

			Thread thread = new Thread(task);
			thread.start();
			
			Registry registry = LocateRegistry.createRegistry(Constants.MCGILL_SERVER_PORT);
			registry.bind(Constants.MCGILL_SERVER_NAME,mcgillStub);
			registry.bind("addItem", mcgillStub);
			registry.bind("removeItem", mcgillStub);
			registry.bind("listItemAvailability", mcgillStub);
			registry.bind("findItem", mcgillStub);
			registry.bind("returnItem", mcgillStub);
			registry.bind("borrowItem", mcgillStub);

			System.out.println("McGill server is started...");
		} catch (RemoteException e) {
			e.printStackTrace();	
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
}
