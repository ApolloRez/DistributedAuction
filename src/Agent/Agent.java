package Agent;

import AuctionHouse.Item;
import shared.AuctionMessage;
import shared.Message;
import shared.NetInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Agent {


    private Socket bankClient = null;
    private Socket auctionClient = null;

    private String bankHostName;
    private int bankPortNumber;

    private String auctionHouse;
    private int auctionPort;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    public AgentDisplay display;

    public boolean run;
    public boolean connectedToBank;
    private boolean connectedToAH;
    private boolean activeBid;

    private double balance;
    private double availableBalance;
    private UUID accountNumber;

    private List<NetInfo> auctionHouses;
    private ArrayList<Item> catalogue = new ArrayList<Item>();

    private Item attemptedBid;
    private Double attemptedBidAmount;
    private ArrayList<Item> currentlyBidding = new ArrayList<Item>();
    private ArrayList<Item> wonItems = new ArrayList<Item>();

    Thread bankInThread;

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

    public String idToString() {
        return accountNumber.toString();
    }

    public Thread getBankInThread() {
        return bankInThread;
    }


    public void registerBank() throws IOException {
        // create a message and send it to the bank using the message class...
        try {
            //System.out.println("Connecting to the bank!");
            System.out.println(bankHostName);
            System.out.println(bankPortNumber);
            bankClient = new Socket(bankHostName, bankPortNumber);
            connectedToBank = true;

            //System.out.println("Connected");
            bankOut = new ObjectOutputStream(bankClient.getOutputStream());
        } catch (IOException u) {
            u.printStackTrace();
            connectedToBank = false;

        }
        Message message = new Message.Builder()
                .command(Message.Command.REGISTER_CLIENT)
                .send(null);
        sendToBank(message);


        run = true;
        //need to give some attention to this

        bankInThread = new Thread(new setBankIn());
        bankInThread.start();
    }

    public void updateBalance() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.GET_AVAILABLE)
                .send(accountNumber);
        sendToBank(message);
    }


    public void bankDeposit(double deposit) throws IOException {
        System.out.println("Sending Deposit");
        Message message = new Message.Builder()
                .command(Message.Command.DEPOSIT)
                .amount(deposit)
                .accountId(accountNumber)
                .send(accountNumber);
        sendToBank(message);

    }

    public void requestAHList() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.GET_NET_INFO)
                .send(accountNumber);
        sendToBank(message);
    }

    public void shutDownWithBank() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.DEREGISTER_CLIENT)
                .accountId(accountNumber)
                .send(accountNumber);
        sendToBank(message);
    }

    public void closeAgent() throws IOException {
        Message message = new Message.Builder()
                .command(Message.Command.DEREGISTER_CLIENT)
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

    public ArrayList<Item> getWonItems() {
        return wonItems;
    }

    public boolean getActiveBid() {
        return true;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public boolean getConnectedToBank() {
        return connectedToBank;
    }


    public class setBankIn implements Runnable {
        public Message message;

        @Override
        public void run() {
            //System.out.println("listening to bank");
            try {
                bankIn = new ObjectInputStream(bankClient.getInputStream());
                while(run) {
                    message = (Message) bankIn.readObject(); //?
                    //System.out.println(message.toString());
                    processBankMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

// process bank message should be able to get the right info from bank

            private void processBankMessage(Message message) throws IOException {
                if (message.getNetInfo() != null) {
                    auctionHouses = message.getNetInfo();
             //       display.printAHList(auctionHouses);
                }
                if (message.getCommand()== Message.Command.REGISTER_CLIENT) {
                    accountNumber = message.getAccountId();
                    System.out.println("got it");
                }
                if (message.getAmount()!= null) {
                    //System.out.println("Balance Updated by Bank");
                    balance = message.getAmount();
                }

                if (message.getResponse() != null) {
                    switch (message.getResponse()) {
                        case SUCCESS: {
                            if (accountNumber == null) {
                                accountNumber = message.getAccountId();
                            }
                            switch (message.getCommand()) {
                                case DEPOSIT: {
                                    //System.out.println("should call print deposit");
                                    //display.printDepositBalance();

                                    break;
                                }
                                case HOLD: {
                                    break;
                                }
                                case RELEASE_HOLD: {
                                    break;

                                }
                                case TRANSFER: {
                                    break;

                                }
                                case GET_AVAILABLE: {
                                    display.printBalance();
                                    break;

                                }

                            }
                            break;
                        }
                        case ERROR: {
                            System.out.println("Something has gone terribly wrong the " +
                                    "bank has made a large error in your favor");
                            break;

                        }
                        case INSUFFICIENT_FUNDS: {
                            System.out.println("Insufficient funds");
                            break;
                        }
                        case INVALID_PARAMETERS: {
                            System.out.println("Invalid Parameters, check the code or the input?");
                            break;
                        }
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
                connectedToAH = true;
                return true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean getConnectedToAH() {
            return connectedToAH;
        }

        public void registerAuctionHouse() throws IOException {
            AuctionMessage message = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.REGISTER).id(accountNumber).build();
            sendToAH(message);
            connectedToAH = true;

        }
        // we need a de register with auction house
        public void deRegisterAuctionHouse() throws IOException {
            AuctionMessage message = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.DEREGISTER)
                    .id(accountNumber).build();
            sendToAH(message);
            connectedToAH = false;

        }

        private void getAuctionNetInfo(int choice) {
            auctionHouse = auctionHouses.get(choice).getIp();
            auctionPort = auctionHouses.get(choice).getPort();
        }

        public void sendBidToAH(int choice, double doubleBid) throws IOException {
            activeBid = true;
            attemptedBid = catalogue.get(choice);
            attemptedBidAmount = doubleBid;
            Double bid = doubleBid;
            AuctionMessage bidMessage = new AuctionMessage.Builder().newB()
                    .type(AuctionMessage.AMType.BID)
                    .item(catalogue.get(choice).getItemID())
                    .amount(bid)
                    .id(accountNumber)
                    .build();
            sendToAH(bidMessage);
        }

        public void getUpdatedCatalogue() throws IOException {
            System.out.println("this isnt printing right??");
            AuctionMessage updateMessage = new AuctionMessage.Builder().newB()
                    .type(AuctionMessage.AMType.UPDATE)
                    .id(accountNumber)
                    .build();
            sendToAH(updateMessage);
        }

    private void sendToAH(AuctionMessage message) throws IOException {
        try {
            auctionOut.writeObject(message);
            System.out.println(message.toString());

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

            private void processAuctionMessage(AuctionMessage message) throws IOException {
                if (message.getType() == AuctionMessage.AMType.ACCEPTANCE) {
                    activeBid = true;
                }
                System.out.println("theres been an update to catatlogue");
                catalogue = message.getCatalogue();
                switch (message.getType()) {
                    case REGISTER: {

                        break;
                    }
                    case OUTBID: {
                        // update which items the agent is know bidding on
                        // if we have a bool about active bids we need to update that too

                        for (Item item : currentlyBidding) {
                            if (message.getItem()== item.getItemID()) {
                                currentlyBidding.remove(item);
                            }
                        }
                        if (currentlyBidding.isEmpty()) {
                            activeBid = false;
                        }
                        break;
                        //display.auctionHouseMenu();
                    }
                    case WINNER: {
                        // need to determine how we are handling "winning an item"
                        //not that important.
                        for (Item item : currentlyBidding) {
                            if (message.getItem()== item.getItemID()) {
                                currentlyBidding.remove(item);
                                wonItems.add(item);
                            }
                            if (currentlyBidding.isEmpty()) {
                                activeBid = false;
                            }
                        }
                        break;
                        //display.wonAnItem();
                        //display.auctionHouseMenu();
                    }
                    case REJECTION: {
                        //print out insufficient funds or so
                        //display.auctionHouseMenu();
                        if (currentlyBidding.isEmpty()) {
                            activeBid = false;
                        }
                        break;
                    }

                    case ACCEPTANCE: {
                        //update which items the agent is now biddin on
                        currentlyBidding.add(attemptedBid);
                        //display.auctionHouseMenu();

                        break;

                    }
                }


            }
        }






    }

