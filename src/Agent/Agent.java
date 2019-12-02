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

    private String bankHostName;
    private int bankPortNumber;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    public AgentDisplay display;

    public boolean connectedToBank;
    private double balance;
    private UUID accountNumber;
    //add a list of items the agent has won.
    private String auctionHouse;
    private int auctionPort;
    private List<NetInfo> auctionHouses;
    private ArrayList<Item> catalogue = new ArrayList<Item>();
    private ArrayList<Item> currentlyBidding = new ArrayList<Item>();
    private ArrayList<Item> wonItems = new ArrayList<Item>();
    private boolean activeBid;


    public Agent(String hostName, int portNumber) {
        bankHostName = hostName;
        bankPortNumber = portNumber;

    }

    public void setDisplay(AgentDisplay display) {
        this.display = display;
    }

    public ArrayList<Item> getCatalogue() {
        return catalogue;
    }

    public UUID getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public List<NetInfo> getAuctionHouses() {
        return auctionHouses;
    }


    public void registerBank() throws IOException {
        // create a message and send it to the bank using the message class...
        try {
            System.out.println("Connecting to the bank!");
            bankClient = new Socket(bankHostName, bankPortNumber);
            System.out.println("Connected");
            bankOut = new ObjectOutputStream(bankClient.getOutputStream());
        } catch (IOException u) {
            u.printStackTrace();
        }
        Message message = new Message.Builder()
                .command(Message.Command.REGISTER_CLIENT)
                .send(null);
        sendToBank(message);
        Thread bankInThread = new Thread(new setBankIn());
        bankInThread.start();
        new setBankIn();
        // need a new thread here right?
    }



    // need too confer with magnus about how to use message to read in the UUID

    //add methods for different bank messages right?


    public void updateBalance() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.GET_AVAILABLE)
                .send(accountNumber);
        sendToBank(message);
    }

    private void sendToBank(Message message) throws IOException {
        try {
            bankOut.writeObject(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bankDeposit(double deposit) throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.DEPOSIT)
                .amount(deposit)
                .send(accountNumber);
        sendToBank(message);
    }

    public ArrayList<Item> getWonItems() {
        return wonItems;
    }


    public class setBankIn implements Runnable {
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
                if (message.getNetInfo() != null) {
                    auctionHouses = message.getNetInfo();
                }
                switch (message.getResponse()) {
                    case SUCCESS: {
                        connectedToBank = true;
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
        }


        public boolean connectToAuctionHouse(int choice) throws IOException {
            getAuctionNetInfo(choice);
            try {
                auctionClient = new Socket(auctionHouse, auctionPort);
                //auctionServer = new ServerSocket(auctionServerPort); // not sure what to init this as
                auctionOut = new ObjectOutputStream(auctionClient.getOutputStream());
                registerAuctionHouse();
                Thread auctionInThread = new Thread(new setAuctionIn());
                auctionInThread.start();
                return true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public void registerAuctionHouse() throws IOException {
            AuctionMessage message = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.REGISTER).id(accountNumber).build();
            auctionOut.writeObject(message);
        }

        public void deRegisterAuctionHouse() {
        }

        private void getAuctionNetInfo(int choice) {
            auctionHouse = auctionHouses.get(choice).getIp();
            auctionPort = auctionHouses.get(choice).getPort();

        }

        public void sendBidToAH(int choice, double doubleBid) throws IOException {
            Double bid = doubleBid;
            AuctionMessage bidMessage = new AuctionMessage.Builder().newB()
                    .type(AuctionMessage.AMType.BID)
                    .item(catalogue.get(choice).getItemID())
                    .amount(bid)
                    .id(accountNumber)
                    .build();
            auctionOut.writeObject(bidMessage);
        }


        public class setAuctionIn implements Runnable {
            public AuctionMessage message;
            public boolean catalogueUpdated;


            @Override
            public void run() {
                catalogueUpdated = false;
                System.out.println("listening to AH");
                try {
                    auctionIn = new ObjectInputStream(auctionClient.getInputStream());
                    message = (AuctionMessage) auctionIn.readObject(); //?
                    processAuctionMessage(message);
                    if (catalogueUpdated) {
                        display.auctionHouseMenu();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            private void processAuctionMessage(AuctionMessage message) {
                if (message.getType() == AuctionMessage.AMType.ACCEPTANCE) {
                    activeBid = true;
                }
                if (message.getCatalogue() != null) {
                    catalogue = message.getCatalogue();
                    catalogueUpdated = true;
                }
                switch (message.getType()) {
                    case OUTBID: {

                    }
                    case WINNER: {
                        // need to determine how we are handling "winning an item"
                        //not that important.
                    }
                    case REJECTION: {

                    }
                    case ACCEPTANCE: {


                    }
                }


            }
        }







        public void closeAgent() {
            // make this two one for de registering with the bank and one for rereg
            // with auction houses/
        }



        public boolean idToString() {
            return false;
        }

        public String getItemString() {
                return null;

        }

        public String getStringOfCurrentFloor() {
            return null;
        }


    }

