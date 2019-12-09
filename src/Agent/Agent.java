package Agent;

import AuctionHouse.Item;
import shared.AuctionMessage;
import shared.Message;
import shared.NetInfo;

import java.io.EOFException;
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
    private UUID auctionID;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    public ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    public AgentDisplay display;

    public boolean run;
    public boolean connectedToBank;
    private boolean connectedToAH;
    private boolean activeBid;


    private double availableBalance;
    private double reservedBalance;

    private UUID accountNumber;

    private List<NetInfo> auctionHouses;
    private ArrayList<Item> catalogue = new ArrayList<Item>();

    private Item attemptedBid;
    private ArrayList<Item> currentlyBidding = new ArrayList<Item>();
    private ArrayList<Item> wonItems = new ArrayList<Item>();

    Thread bankInThread;
    Thread auctionInThread;

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

        return availableBalance + reservedBalance;
    }

    public List<NetInfo> getAuctionHouses() {
        return auctionHouses;
    }

    public String idToString() {
        return accountNumber.toString();
    }


    public void registerBank() throws IOException {
        try {
            activeBid = false;
            availableBalance = 0;
            reservedBalance = 0;
            System.out.println(bankHostName);
            System.out.println(bankPortNumber);
            bankClient = new Socket(bankHostName, bankPortNumber);
            connectedToBank = true;
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

    public void updateBalance() {
        System.out.println("Should be requesting updated sums");
        Message message = new Message.Builder()
                .command(Message.Command.GET_AVAILABLE)
                .send(accountNumber);
        sendToBank(message);
        Message message1 = new Message.Builder()
                .command(Message.Command.GET_RESERVED)
                .send(accountNumber);
        sendToBank(message1);
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

    public void shutDownWithBank() {
        Message message = new Message.Builder()
                .command(Message.Command.DEREGISTER_CLIENT)
                .accountId(accountNumber)
                .send(accountNumber);
        sendToBank(message);
    }

    private void sendToBank(Message message) {
        System.out.println(message.toString());
        try {
            bankOut.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transferFunds(Double amount) {
        Message message = new Message.Builder()
                .command(Message.Command.TRANSFER)
                .amount(amount)
                .accountId(auctionID)
                .send(accountNumber);
        sendToBank(message);

    }

    public ArrayList<Item> getWonItems() {
        return wonItems;
    }

    public boolean getActiveBid() {
        return activeBid;
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
            try {
                bankIn = new ObjectInputStream(bankClient.getInputStream());
                while(run) {
                    message = (Message) bankIn.readObject(); //?
                    processBankMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

// process bank message should be able to get the right info from bank

            private void processBankMessage(Message message) throws IOException {
                System.out.println(message.toString());
                if (message.getNetInfo() != null) {
                    auctionHouses = message.getNetInfo();
                }
                /*
                if (message.getCommand().equals(Message.Command.REGISTER_CLIENT)) {
                    accountNumber = message.getAccountId();
                    System.out.println("got it");
                }
                */



                    if (message.getResponse() != null) {
                    switch (message.getResponse()) {
                        case SUCCESS: {
                            switch (message.getCommand()) {
                                case REGISTER_CLIENT: {
                                    accountNumber = message.getAccountId();
                                    break;

                                }
                                case DEPOSIT: {
                                    availableBalance = availableBalance + message.getAmount();
                                    break;
                                }
                                case DEREGISTER_CLIENT: {
                                    bankIn.close();
                                    bankOut.close();
                                    bankInThread.stop();
                                    bankClient.close();
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
                    switch (message.getCommand()) {

                         case GET_AVAILABLE: {
                            availableBalance = message.getAmount();
                            break;
                        }
                        case GET_RESERVED: {
                            System.out.println("Say something Im giving up on you");
                            reservedBalance = message.getAmount();
                            System.out.println("reserved Balance: "+ reservedBalance);
                            break;
                    }

                }
            }
        }


        public boolean connectToAuctionHouse(int choice) throws IOException {
            getAuctionNetInfo(choice);
            try {
                auctionClient = new Socket(auctionHouse, auctionPort);
                auctionOut = new ObjectOutputStream(auctionClient.getOutputStream());
                registerAuctionHouse();
                auctionInThread = new Thread(new setAuctionIn());
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

        public void registerAuctionHouse() {
            AuctionMessage message = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.REGISTER).id(accountNumber).build();
            sendToAH(message);

        }
        public void deRegisterAuctionHouse() {
            AuctionMessage message = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.DEREGISTER)
                    .id(accountNumber).build();
            sendToAH(message);
            System.out.println("got a dereg from the AH");
            try {
                connectedToAH = false;
                auctionIn.close();
                auctionOut.close();
                auctionInThread.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void getAuctionNetInfo(int choice) {
            auctionHouse = auctionHouses.get(choice).getIp();
            auctionPort = auctionHouses.get(choice).getPort();
        }

        public void sendBidToAH(int choice, double doubleBid) throws IOException {
            getBalance();
            activeBid = true;
            attemptedBid = catalogue.get(choice);
            Double bid = doubleBid;
            AuctionMessage bidMessage = new AuctionMessage.Builder().newB()
                    .type(AuctionMessage.AMType.BID)
                    .item(catalogue.get(choice).getItemID())
                    .amount(bid)
                    .id(accountNumber)
                    .build();
            sendToAH(bidMessage);

        }

        public void getUpdatedCatalogue() {
            AuctionMessage updateMessage = new AuctionMessage.Builder().newB()
                    .type(AuctionMessage.AMType.UPDATE)
                    .id(accountNumber)
                    .build();
            sendToAH(updateMessage);
        }

    private void sendToAH(AuctionMessage message) {
        try {
            auctionOut.writeObject(message);
            System.out.println(message.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public class setAuctionIn implements Runnable {
            public AuctionMessage message;
            private String bidStatus;


            @Override
            public void run() {
                System.out.println("listening to AH");
                try {
                    bidStatus = "";
                    auctionIn = new ObjectInputStream(auctionClient.getInputStream());
                    System.out.println("Connected to AH: "+getConnectedToAH());
                    while(getConnectedToAH()) {
                        System.out.println("is this? " +getConnectedToAH());

                        message = (AuctionMessage) auctionIn.readObject(); //?
                        System.out.println(message.toString());
                        processAuctionMessage(message);
                    }
                } catch (IOException | ClassNotFoundException  e) {
                    e.printStackTrace();
                }
            }

            public String getBidStatus (){

                return bidStatus;
            }

            private void processAuctionMessage(AuctionMessage message) {

                switch (message.getType()) {
                    case REGISTER: {
                        catalogue = message.getCatalogue();
                        auctionID = message.getId();
                        break;
                    }
                    case UPDATE: {

                        catalogue = message.getCatalogue();
                        break;
                    }
                    case OUTBID: {
                        for (Item item : currentlyBidding) {
                            if (message.getItem().equals(item.getItemID())) {
                                currentlyBidding.remove(item);
                            }
                        }
                        if (currentlyBidding.isEmpty()) {
                            activeBid = false;
                        }
                        break;
                    }
                    case WINNER: {
                        transferFunds(message.getAmount());
                        for (Item item : currentlyBidding) {
                            if (message.getItem().equals(item.getItemID())) {
                                currentlyBidding.remove(item);
                                wonItems.add(item);
                            }
                            if (currentlyBidding.isEmpty()) {
                                activeBid = false;
                                break;
                            }
                            System.out.println(currentlyBidding.size());
                            System.out.println(activeBid);
                        }

                        break;
                    }
                    case REJECTION: {
                        if (currentlyBidding.isEmpty()) {
                            activeBid = false;
                        }
                        break;
                    }
                    case ACCEPTANCE: {
                        currentlyBidding.add(attemptedBid); //969526df-e6b6-4fc0-a3ef-1f53206de975
                        break;

                    }
                    case DEREGISTER: {

                    }
                }


            }
        }






    }

