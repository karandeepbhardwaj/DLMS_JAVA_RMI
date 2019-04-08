package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import ConstantValue.Constants;
import dlmsServerInterface.AddInterface;

public class Client {

	public void allUsers(String inputId,Registry registry) throws NotBoundException, IOException {
		AddInterface interfaceObject;
		String itemName = null;
		String itemID = null;
		System.out.println("Welcome to Library: "+ inputId + "\n"+"Please make a choice from below :");
		System.out.println("1. Find a book.");
		System.out.println("2. Borrow a book.");
		System.out.println("3. Return a book.");
		System.out.println("4. Exit. \n");

		BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
		int choice = Integer.parseInt(inp.readLine());
		try {
			switch(choice) {

			case 1:
				interfaceObject = (AddInterface)registry.lookup("findItem");
				System.out.println("Enter the book name: ");
				itemName = inp.readLine();
				String str = interfaceObject.findItem(inputId, itemName);
				System.out.println(str);
				break;

			case 2:
				interfaceObject = (AddInterface)registry.lookup("borrowItem");
				System.out.println("Please enter book ID: ");
				itemID = inp.readLine();
				String responseString = interfaceObject.borrowItem(inputId, itemID);
				if(responseString.trim().equals("waitlist")) {
					System.out.println("Book not available in the library, Do you want to get in the waiting list ?");
					System.out.println("1. Yes");
					System.out.println("2. No");
					int ch = Integer.parseInt(inp.readLine());

					switch (ch) {
					case 1: interfaceObject.waitingList(inputId, itemID);
					System.out.println("Successfully added to the list");
					break;
					case 2: System.out.println("Have a nice day");
					}					
				}else {
					System.out.println(responseString);
				}
				break;
			case 3:
				interfaceObject = (AddInterface)registry.lookup("returnItem");
				System.out.print("Please enter book ID: ");
				itemID = inp.readLine();
				responseString = interfaceObject.returnItem(inputId, itemID);
				System.out.println(responseString);
				break;
				
			case 4:
				System.out.println("Exit");
				System.exit(0);
				break;
				
			default:
				System.out.println("Please enter valid input"+ "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void allManagers(String inputId, Registry registry) throws NotBoundException, IOException {
		AddInterface interfaceObject;
		int quantity =0;
		int option = 0;
		String itemName = null;
		String itemID = null;
		System.out.println("Welcome to Library: "+inputId+" Please make a choice from below.");
		System.out.println("1. Add a book");
		System.out.println("2. Remove a book.");
		System.out.println("3. List available books in the Library.");
		System.out.println("4. Exit");

		BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
		int choice = Integer.parseInt(inp.readLine());

		switch(choice) {

		case 1:
			interfaceObject= (AddInterface)registry.lookup("addItem");
			System.out.println("Please enter the name of the book");
			itemName = inp.readLine();

			do {
				System.out.println("Please enter ID: ");	
				itemID = inp.readLine();
			} while (!inputId.substring(0, 3).equals(itemID.substring(0, 3)));

			System.out.println("Please enter quantity: ");
			quantity = Integer.parseInt(inp.readLine());

			String result = interfaceObject.addItem(inputId, itemID, itemName, quantity);
			System.out.println(result);
			break;

		case 2:
			interfaceObject =  (AddInterface)registry.lookup("removeItem");
			System.out.println("Please specify action to perform : ");
			System.out.println("1. Remove item completely? ");
			System.out.println("2. Decrease the quantity of the item");
			option = Integer.parseInt(inp.readLine());


			System.out.println("Please enter book ID: ");
			itemID = inp.readLine();
			if(option != 1) {
				System.out.println("Please enter quantity: ");
				quantity = Integer.parseInt(inp.readLine());
			}

			String str =interfaceObject.removeItem(inputId, itemID, quantity, option);
			System.out.println(str);
			break;

		case 3:
			interfaceObject =  (AddInterface)registry.lookup("listItemAvailability");
			ArrayList<String> listResult = interfaceObject.listItemAvailability(inputId);
			for(String string : listResult) {
				System.out.println(string);
			}
			break;
			
		case 4:
			System.out.println("Exit");
			System.exit(0);
			break;

		default:
			System.out.println("Invalid number,Please try again.");
		}
	}
	public static void main(String[] args) throws Exception {

		try {

			Registry rconcordia = LocateRegistry.getRegistry(Constants.CONCORDIA_SERVER_PORT);
			Registry rmcgill = LocateRegistry.getRegistry(Constants.MCGILL_SERVER_PORT);
			Registry rmontreal = LocateRegistry.getRegistry(Constants.MONTREAL_SERVER_PORT);

			Client client = new Client();
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));

			System.out.print("Please enter your ID: ");
			String inputId = inp.readLine();

			if(inputId.substring(3, 4).contains("U")) {
				String check = inputId.substring(0,3);
				switch(check) {
				case "CON": client.allUsers(inputId,rconcordia);
				break;
				case "MCG": client.allUsers(inputId,rmcgill);
				break;
				case "MON": client.allUsers(inputId,rmontreal);
				break;
				default:
					System.out.println("Invalid Input.");
				}
			}else if(inputId.substring(3, 4).contains("M")) {
				String check = inputId.substring(0,3);
				switch(check) {
				case "CON": client.allManagers(inputId,rconcordia);
				break;
				case "MCG": client.allManagers(inputId,rmcgill);
				break;
				case "MON": client.allManagers(inputId,rmontreal);
				break;
				default:
					System.out.println("Invalid Input.");
				}
			}else {
				System.out.println("Re-enter the Id. Entered id is invalid.");
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}catch (NotBoundException e) {
			e.printStackTrace();
		} 	
	}
}