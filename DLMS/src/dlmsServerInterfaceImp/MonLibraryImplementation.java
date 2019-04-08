package dlmsServerInterfaceImp;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import SendReceive.SendReceiveImplementation;
import dlmsServerInterface.AddInterface;
import java.util.ArrayList;

public class MonLibraryImplementation extends UnicastRemoteObject implements AddInterface {

	private static final long serialVersionUID = 1L;

	public MonLibraryImplementation() throws RemoteException {
		super();
	}

	Map<String, HashMap<String, Integer>> outerMap = new HashMap<>();
	HashMap<String, List<String>> bookMap = new HashMap<>();
	HashMap<String, List<String>> bookQueue = new HashMap<>();
	HashMap<String, Integer> innerMap = new HashMap<>();

	@Override
	public String addItem(String managerID, String itemID, String itemName, int quantity) throws RemoteException {
		int qty = 0;
		if(outerMap == null || !outerMap.containsKey(itemID)) {
			innerMap.put(itemName, quantity);
			outerMap.put(itemID, innerMap);
			innerMap = new HashMap<String, Integer>();
			return "Item added succesfully";
		} else if(outerMap.containsKey(itemID)){
			innerMap = outerMap.get(itemID);
			qty = innerMap.get(itemName);
			qty = qty + quantity;
			innerMap.put(itemName,qty);
			outerMap.put(itemID,innerMap);
			return "Item Updated succesfully";
		}
		while(bookQueue.containsKey(itemID) && qty != 0) {
			List<String> userID = bookQueue.get(itemID);
			String userId=userID.get(0);
			try {
				borrowItem(userId, itemID);
				userID.remove(userId);
				qty--;
				if (userID.size() == 0) {
					bookQueue.remove(itemID);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "Invalid input";
	}
	@Override
	public String removeItem(String managerID, String itemID, int quantity, int option) throws RemoteException {
		HashMap<String, Integer> innerMap = new HashMap<>();
		int qty = 0;
		String itemN = null;
		if(option == 1) {
			outerMap.remove(itemID);
			return "Book removed succesfully";
		} else if(option == 2) {
			for(String str : outerMap.get(itemID).keySet()) {
				qty = outerMap.get(itemID).get(str);
				itemN = str;
			}
			qty-=quantity;
			innerMap.put(itemN, qty);
			outerMap.put(itemID, innerMap);
			return "Book number decreased successfully";
		}
		return " Invalid input";
	}

	@Override
	public ArrayList<String> listItemAvailability(String managerID) throws RemoteException {
		ArrayList<String> list = new ArrayList<String>();

		for (String itemID : outerMap.keySet()) {
			String str = itemID + " : ";

			for (String itemName : outerMap.get(itemID).keySet()) {
				System.out.print(itemName + " " + outerMap.get(itemID).get(itemName));
				str +=  itemName + " / " + outerMap.get(itemID).get(itemName)+"\n";
			}
			list.add(str); 
		}
		return list;
	}
	@Override
	public String borrowItem(String userID, String itemID) throws RemoteException, IOException {
		String responseString =null;
		int qty = 0;
		String name = null;
		if(!userID.substring(0, 3).equals("MON")) {
			for(String str : bookMap.keySet()) {
				if (bookMap.get(str).contains(userID)) {
					return "Cannot take more than one book";
				}
			}
		}
		if(itemID.substring(0,3).contains("MON")){
			if(bookMap.containsKey(itemID) && bookMap.get(itemID).contains(userID)) {
				return "You have already taken this book, cant be issued twice!";
			}
			if(outerMap.containsKey(itemID)) {
				innerMap = outerMap.get(itemID);
				for(String str : innerMap.keySet()) {
					qty = innerMap.get(str);
					name = str;
				}
				if(qty != 0) {
					qty--;
					innerMap.put(name,qty);
					outerMap.put(itemID, innerMap);
					if(bookMap.containsKey(itemID)) {
						bookMap.get(itemID).add(userID);
					}else {
						List<String> borredList = new ArrayList<String>();
						borredList.add(userID);
						bookMap.put(itemID, borredList);
					}
					responseString = "Book issued successfully";
				}  else {
					return "waitlist";
				}
			}else {
				responseString = "Book doesnot exist";
			}
		}else{
			SendReceiveImplementation serverObj = new SendReceiveImplementation();
			responseString = serverObj.sendMessage(userID, itemID, 1, "");
		}
		return responseString;
	}

	@Override
	public String findItem(String userID, String itemName) throws RemoteException {
		String responseString = "";
		String itemID = null;
		Iterator<Entry<String, HashMap<String, Integer>>> mapData = outerMap.entrySet().iterator();
		while (mapData.hasNext()) {
			System.out.println(mapData);
			Entry<String, HashMap<String, Integer>> data = mapData.next();
			itemID = data.getKey();
			innerMap = data.getValue();
			if(innerMap.containsKey(itemName)) {
				responseString += itemID+" / "+innerMap.get(itemName);
				return responseString;
			}
		}
		SendReceiveImplementation serverObj = new SendReceiveImplementation();
		responseString = serverObj.sendMessage(userID,itemID, 2, itemName);
		return responseString;
	}
	@Override
	public String returnItem(String userID, String itemID) throws IOException {
		String responseString;
		if(itemID.substring(0,3).contains("MON")) {
			if(bookMap.containsKey(itemID) && !bookMap.get(itemID).contains(userID)) {
				String s = "Book from the borrower needs to be returned";
				return s;
			}
			int qty = 0;
			String itemName = "";
			innerMap = outerMap.get(itemID);
			for(String str : innerMap.keySet()) {
				qty = innerMap.get(str);
				itemName = str;
			}
			innerMap.put(itemName, qty + 1);
			outerMap.put(itemID, innerMap);
			bookMap.get(itemID).remove(userID);
			responseString = "Book submitted";
		}else {
			SendReceiveImplementation serverObj = new SendReceiveImplementation();
			responseString = serverObj.sendMessage(userID, itemID, 3, "");
		}
		if(bookQueue.containsKey(itemID)) {
			String waitingListUser = bookQueue.get(itemID).get(0);
			borrowItem(waitingListUser, itemID);
			bookQueue.get(itemID).remove(waitingListUser);
			if(bookQueue.get(itemID).size() == 0) {
				bookQueue.remove(itemID);
			}
		}
		return responseString;
	}
	public String waitingList(String userID, String itemID) throws RemoteException{
		List<String> waitingList;
		if(bookQueue.containsKey(itemID)) {
			waitingList = bookQueue.get(itemID);
			if(waitingList.contains(userID)) {
				return "User already in the Waiting list";
			}else {
				waitingList.add(userID);
			}
		}else {
			waitingList = new ArrayList();
			waitingList.add(userID);
			bookQueue.put(itemID,waitingList);
		}
		return "This is me";
	}
}