package Agent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Agent {


    private ServerSocket BankServer = null;
    private ServerSocket AuctionServer = null;
    private Socket client = null;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    public boolean connectedToBank;
    private double balance;
    private int accountNumber;
    //add a list of items the agent has won.


    public Agent(String hostName, int portNumber) {
        try {
            client = new Socket(hostName,portNumber);
            System.out.println("Connected");
            bankOut = new ObjectOutputStream(client.getOutputStream());
        } catch(IOException u) {
            u.printStackTrace();
        }
    }

    public void registerBank() {


    }



    public void connectAuctionServer() throws IOException {
        try (Socket socket = new Socket(hostName, portNumber);
             auctionIn = new ObjectInputStream(socket.getOutputStream(), true));
             auctionOut = new ObjectInputStream(new InputStreamReader(socket.getInputStream()))
        )
        {
            registerAuctionHouse();
        }
             // use object output stream
        {

        }

    }

    public boolean connectToAuctionHouse(int choice) {
        hostName
        try (Socket socket = new Socket(hostName, portNumber);

        return false;
    }

    public AuctionENUMs makeBid(/*Item to bid on and price?*/) {
        /*
        make a bid on an item at the auction house and return
        one of the ENUM regarding the result.
         */
        return null;
    }



    public void registerAuctionHouse() {

    }



    public static void main(String[] args) {


    }




    public void closeAgent() {
    }

    public void bankDeposit(int deposit) {
    }

    public LinkedList<String> getAuctionList() {
        return null;
    }

    public double getBalance() {
    }

    public boolean idToString() {
    }

    public String getItemString() {
    }

    public String getStringOfCurrentFloor() {
    }
}
