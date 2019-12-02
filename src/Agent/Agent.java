package Agent;

import AuctionHouse.Item;
import shared.AuctionMessage;
import shared.Message;
import shared.NetInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
    private List<NetInfo> auctionHouses;
    private ArrayList<Item> catalogue;
    private ArrayList<Item> currentlyBidding;
    private ArrayList<Item> wonItems;
    private boolean activeBid;



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
        Message message = new Message.Builder()
                .command(Message.Command.REGISTER_CLIENT)
                .send(null);
        sendToBank(message);
        new setBankIn();
    // need a new thread here right?
    }

    private void sendToBank(Message message) throws IOException {
        try {
            bankOut.writeObject(message);

        } catch(IOException e) {
        e.printStackTrace();
        }
    }

    public ArrayList<Item> getCatalogue() {
        return catalogue;
    }

    public UUID getAccountNumber() {
        return accountNumber;
    }

    public void deRegisterAuctionHouse() {
    }

    public class setBankIn implements Runnable{
        public Message message;
        @Override
        public void run() {
            System.out.println("listening to bank");
            try {
                bankIn = new ObjectInputStream(bankClient.getInputStream());
                message = (Message) bankIn.readObject(); //?
                processBankMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
        private void processBankMessage(Message message) {
            if (message.getAccountId()!=null && accountNumber==null) {
                accountNumber = message.getAccountId();
            }
            if (message.getResponse() == Message.Response.SUCCESS) {
                AgentDisplay.printSuccess();
            }
            if (message.getNetInfo()!=null) {
                auctionHouses = message.getNetInfo();
            }
            if (message.getAmount() != null) {
                balance = message.getAmount();
            }
        }
*/

        private void processBankMessage(Message message) {
            if (message.getNetInfo()!=null) {
                auctionHouses = message.getNetInfo();
            }
            switch (message.getResponse()) {
                case SUCCESS: {
                    accountNumber = message.getAccountId();
                    balance = message.getAmount();

                }
                case ERROR: {
                    System.out.println("Something has gone terribly wrong the " +
                            "bank has made a large error in your favor");

                }
                case INSUFFICIENT_FUNDS: {
                    System.out.println("Insufficient funds");
                }
                case INVALID_PARAMETERS: {
                    System.out.println("Invalid Parameters, check the code or the input?");
                }
            }
        }
        private void printAgentBalance() {

        }

        // need too confer with magnus about how to use message to read in the UUID

        //add methods for different bank messages right?
    }




    public boolean connectToAuctionHouse(int choice) throws IOException {
        getAuctionNetInfo(choice);
        try {
            auctionClient = new Socket(auctionHouse,auctionPort);
            //auctionServer = new ServerSocket(auctionServerPort); // not sure what to init this as
            auctionOut = new ObjectOutputStream(auctionClient.getOutputStream());
            registerAuctionHouse();
            Thread auctionInThread = new Thread(new setAuctionIn());
            auctionInThread.start();
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class setAuctionIn implements Runnable {
        public AuctionMessage message;

        @Override
        public void run() {
            System.out.println("listening to AH");
            try {
                auctionIn = new ObjectInputStream(auctionClient.getInputStream());
                message = (AuctionMessage) auctionIn.readObject(); //?
                processAuctionMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void processAuctionMessage(AuctionMessage message) {
            if (message.getType()==AuctionMessage.AMType.ACCEPTANCE) {
                activeBid = true;
            }
            if (message.getCatalogue() != null) {
                catalogue = message.getCatalogue();
            }
            switch (message.getType()) {
                case OUTBID: {

                }
                case WINNER: {
                    //comms with bank go here about winning???
                }
                case REJECTION: {

                }
                case ACCEPTANCE: {

                }
            }


        }
    }

        private void getAuctionNetInfo(int choice) {
        auctionHouse = auctionHouses.get(choice).getIp();
        auctionPort = auctionHouses.get(choice).getPort();

    }




    public void registerAuctionHouse() throws IOException {
        AuctionMessage message = AuctionMessage.Builder.newB()
                .type(AuctionMessage.AMType.REGISTER).id(accountNumber).build();
        auctionOut.writeObject(message);
    }

    public void sendBidToAh(int choice, double bid) {

    }



    public static void main(String[] args) {
        // starts the display along???


    }




    public void closeAgent() {
        // make this two one for de registering with the bank and one for rereg
        // with auction houses/
    }

    public void bankDeposit(double deposit) throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.DEPOSIT)
                .amount(deposit)
                .send(accountNumber);
        sendToBank(message);
    }



    public void updateBalance() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.GET_AVAILABLE)
                .send(accountNumber);
        sendToBank(message);
    }

    public double getBalance() {
        return balance;
    }

    public boolean idToString() {
    }

    public String getItemString() {
    }

    public String getStringOfCurrentFloor() {
    }

    public List<NetInfo> getAuctionHouses() {
        return auctionHouses;
    }
}
