package Agent;

import shared.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.UUID;

public class Agent {


    private ServerSocket bankServer = null;
    private ServerSocket auctionServer = null;
    private Socket bankClient = null;
    private Socket auctionClient = null;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    public boolean connectedToBank;
    private double balance;
    private UUID accountNumber;
    //add a list of items the agent has won.
    private String auctionHouse;
    private int auctionPort;


    public Agent(String hostName, int portNumber) {
        try {
            bankClient = new Socket(hostName,portNumber);
            System.out.println("Connected");
            bankOut = new ObjectOutputStream(bankClient.getOutputStream());
        } catch(IOException u) {
            u.printStackTrace();
        }
    }

    public void registerBank() throws IOException {
        // create a message and send it to the bank using the message class...
    Message.Builder register = new Message.Builder();
    register.command(Message.Command.REGISTER_CLIENT);
    Message registerMessage = new Message(register);
    bankOut.writeObject(registerMessage);
    new setBankIn();
    // need a new thread here right?
    }

    public class setBankIn implements Runnable{
        public Message message;
        @Override
        public void run() {
            System.out.println("listening to bank");
            try {
                bankIn = new ObjectInputStream(bankClient.getInputStream());
                message = (Message) bankIn.readObject(); //?
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        // need too confer with magnus about how to use message to read in the UUID

        //add methods for different bank messages right?
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

    public boolean connectToAuctionHouse(int choice) throws IOException {
        getAuctionNetInfo(choice);
        try {
            auctionClient = new Socket(auctionHouse,auctionPort);

            auctionServer = new ServerSocket(auctionServerPort);



        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getAuctionNetInfo(int choice) {
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
