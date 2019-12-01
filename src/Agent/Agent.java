package Agent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Agent {

    public boolean connectedToBank;
    private ServerSocket BankServer = null;
    private ServerSocket AuctionServer = null;
    private Socket client = null;
    private double balance;
    private int accountNumber;
    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    //add a list of items the agent has won.


    public Agent(String hostName, int portNumber) {

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in =
                     new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer = in.readLine();
            while(fromServer != null) {
                System.out.println("Bank: ");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connectAuctionServer(String hostName, int portNumber) throws IOException {
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

    public boolean connectToAuctionHouse(int choice/*IP or somethin?*/) {
        /*
        "The agent gets a list of active auction houses from the bank.
         In connects to an auction
        house using the host and port information sent from the bank."
         */
        return false;
    }

    public AuctionENUMs makeBid(/*Item to bid on and price?*/) {
        /*
        make a bid on an item at the auction house and return
        one of the ENUM regarding the result.
         */
        return null;
    }

    public void contactBank(AuctionENUMs result) {
        /*
        if the agent win it must contact the bank to unblock and transfer the funds.

        this might not be the exact best method to handle all of the results.
         */
    }

    public void registerAuctionHouse() {

    }



    public static void main(String[] args) {


    }


    public void registerBank() {
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
}
