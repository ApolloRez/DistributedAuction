package Agent;

import AuctionHouse.Item;
import shared.AuctionMessage;
import shared.Message;
import shared.NetInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Agent {
    /**
     * bankClient - the client socket for connecting to the bank
     * auctionClient - the client socket for connecting to an auction house
     * bankHostName - the bank's ip
     * bankPortNumber - the bank's port number
     * auctionHouse - the AH's ip
     * auctionPort - the AH's port number
     * auctionID - the auction house UUID
     * bankIn/bankOut - object in/out stream for the bank
     * auctionIn/auctionOut - object in/out stream for the bank
     * run - bool for running
     * connectedToBank - bool for connection to Bank
     * connectedToAH - bool for connection to AH
     * activeBid - bool for if the agent is currently bidding
     * bidStatus - string to give the current bidStatus
     * availableBalance - available balance in account
     * reservedBalance - held balance in account
     * accountNumber - the agent's unique UUID for communication
     * auctionHouses - netInfo of online AHs
     * catalogue - list of items for sale by the current AH
     * attemptedBid - the item the agent most recently bid on
     * currentlyBidding - the list of items the agent is currently bidding on,
     *     mostly to track the agent's active bid status
     * wonItems - list of items the agent has won, isn't implemented
     * auction/bankInThread - threads for the AH and Bank input
     */


    private Socket bankClient = null;
    private Socket auctionClient = null;

    private String bankHostName;
    private int bankPortNumber;

    private String auctionHouse;
    private int auctionPort;
    private UUID auctionID;

    private ObjectInputStream bankIn;
    private ObjectOutputStream bankOut;
    private ObjectInputStream auctionIn;
    private ObjectOutputStream auctionOut;

    private boolean run;
    private boolean connectedToBank;
    private boolean connectedToAH;
    private boolean activeBid = false;

    private String bidStatus;

    private double availableBalance;
    private double reservedBalance;

    private UUID accountNumber;

    private List<NetInfo> auctionHouses;
    private ArrayList<Item> catalogue = new ArrayList<Item>();

    private Item attemptedBid;
    private ArrayList<AuctionHouse.Item> currentlyBidding = new ArrayList<Item>();
    private ArrayList<Item> wonItems = new ArrayList<Item>();

    Thread bankInThread;
    Thread auctionInThread;

    /**
     * constructs the agent with ip and port number of the bank
     * @param hostName
     * @param portNumber
     */
    public Agent(String hostName, int portNumber) {
        bankHostName = hostName;
        bankPortNumber = portNumber;
    }


    /**
     * returns the stored catalogue
     * @return
     */
    public ArrayList<Item> getCatalogue() {
        return catalogue;
    }

    /**
     * returns the available + reserved balance
     * @return
     */
    public double getBalance() {

        return availableBalance + reservedBalance;
    }

    /**
     * returns the bidStatus String
     * @return
     */
    public String getBidStatus() {
        return bidStatus;
    }

    /**
     * returns the list of auction houses net info
     * @return
     */
    public List<NetInfo> getAuctionHouses() {
        return auctionHouses;
    }

    /**
     * this registers the agent with the bank.
     * creates the socket, sets the output stream, and sends the first message
     *      to the bank.
     * Finally it starts the bankIn thread.
     */
    public void registerBank() {
        try {
            availableBalance = 0;
            reservedBalance = 0;
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

        bankInThread = new Thread(new setBankIn());
        bankInThread.start();
    }

    /**
     * sends two messages to the bank, the first requests the available balance
     *     the second requests the reserved balance
     */
    public void updateBalance() {
        Message message = new Message.Builder()
                .command(Message.Command.GET_AVAILABLE)
                .send(accountNumber);
        sendToBank(message);
        Message message1 = new Message.Builder()
                .command(Message.Command.GET_RESERVED)
                .send(accountNumber);
        sendToBank(message1);
    }


    /**
     * sends a deposit message to the bank with a given double amount
     * @param deposit - the amount of funds to be deposited
     */
    public void bankDeposit(double deposit) {
        Message message = new Message.Builder()
                .command(Message.Command.DEPOSIT)
                .amount(deposit)
                .accountId(accountNumber)
                .send(accountNumber);
        sendToBank(message);
    }

    /**
     * requests the netInfo for future connection to auction houses
     */
    public void requestAHList() {
        Message message = new Message.Builder()
                .command(Message.Command.GET_NET_INFO)
                .send(accountNumber);
        sendToBank(message);
    }

    /**
     * sends de-register message to the bank
     */
    public void shutDownWithBank() {
        Message message = new Message.Builder()
                .command(Message.Command.DEREGISTER_CLIENT)
                .accountId(accountNumber)
                .send(accountNumber);
        sendToBank(message);
    }

    /**
     * sends a message to the bank.
     * @param message the message
     */
    private void sendToBank(Message message) {
        try {
            bankOut.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * transfer funds to the auction house after winning a bid
     * @param amount the amount to transfer
     */
    private void transferFunds(Double amount) {
        Message message = new Message.Builder()
                .command(Message.Command.TRANSFER)
                .amount(amount)
                .accountId(auctionID)
                .send(accountNumber);
        sendToBank(message);

    }

    /**
     * returns if the agent has an active bid
     * @return
     */
    public boolean getActiveBid() {
        return activeBid;
    }

    /**
     * returns the available balance
     * @return
     */
    public double getAvailableBalance() {
        return availableBalance;
    }

    /**
     * returns true if the agent is connected to the bank
     * @return
     */
    public boolean getConnectedToBank() {
        return connectedToBank;
    }

    /**
     * setBankIn implements runnable in order to listen and read messages from
     *     the bank.
     */
    public class setBankIn implements Runnable {

        /**
         * sets the input stream and then calls the methods to process said message.
         */
        @Override
        public void run() {
            try {
                bankIn = new ObjectInputStream(bankClient.getInputStream());
                while(run) {
                    Message message = (Message) bankIn.readObject();
                    processBankMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        /**
         * gets the message from the input and makes sure the appropriate
         *     information gets to the right places.
         * @param message
         * @throws IOException due to close.
         */
        private void processBankMessage(Message message) throws IOException {
            if (message.getNetInfo() != null) {
                auctionHouses = message.getNetInfo();
            }
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
                }
            }
            if (message.getCommand() != null) {
                switch (message.getCommand()) {
                    case GET_AVAILABLE: {
                        availableBalance = message.getAmount();
                        break;
                    }
                    case GET_RESERVED: {
                        reservedBalance = message.getAmount();
                        break;
                    }
                }
            }
        }
    }

    /**
     * takes an integer and connects the agent to the corresponding auction house,
     *     starts the auctionIn thread
     * @param choice
     * @return
     */
    public boolean connectToAuctionHouse(int choice) {
        getAuctionNetInfo(choice);
        try {
            auctionClient = new Socket(auctionHouse, auctionPort);
            auctionOut = new ObjectOutputStream(auctionClient.getOutputStream());
            registerAuctionHouse();
            auctionInThread = new Thread(new setAuctionIn());
            auctionInThread.start();
            connectedToAH = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * returns true if the agent is connected to an AH
     * @return
     */
    public boolean getConnectedToAH() {
        return connectedToAH;
    }

    /**
     * creates a register message and sends it in order to register the agent
     */
    public void registerAuctionHouse() {
        AuctionMessage message = AuctionMessage.Builder.newB()
                .type(AuctionMessage.AMType.REGISTER).id(accountNumber).build();
        sendToAH(message);
    }

    /**
     * creates a de-register method and send it to deregister the agent
     *     also closes the auction in/out, client and inThread
     */
    public void deRegisterAuctionHouse() {
        AuctionMessage message = AuctionMessage.Builder.newB()
                .type(AuctionMessage.AMType.DEREGISTER)
                .id(accountNumber).build();
        sendToAH(message);
        try {
            connectedToAH = false;
            auctionIn.close();
            auctionOut.close();
            auctionInThread.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sets the stored netinfo for a chosen auction house
     * @param choice
     */
    private void getAuctionNetInfo(int choice) {
        auctionHouse = auctionHouses.get(choice).getIp();
        auctionPort = auctionHouses.get(choice).getPort();
    }

    /**
     * sends a bid to the auction house, requires an integer for which item
     *     and a double for the amount of the bid.
     * @param choice
     * @param doubleBid
     * @throws IOException
     */
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

    /**
     * requests an updated catalogue from the AH.
     */
    public void getUpdatedCatalogue() {
        AuctionMessage updateMessage = new AuctionMessage.Builder().newB()
                .type(AuctionMessage.AMType.UPDATE)
                .id(accountNumber)
                .build();
        sendToAH(updateMessage);
    }

    /**
     * sends an Auction Message to the Auction House
     * @param message
     */
    private void sendToAH(AuctionMessage message) {
        try {
            auctionOut.writeObject(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this implements runnable to listen for and interpret communications from
     *     the auction house.
     */
    public class setAuctionIn implements Runnable {
        public AuctionMessage message;


        /**
         * starts the input stream and gets a message, then sends it to process message.
         */
        @Override
        public void run() {
            try {
                bidStatus = "";
                auctionIn = new ObjectInputStream(auctionClient.getInputStream());
                while(getConnectedToAH()) {
                    message = (AuctionMessage) auctionIn.readObject(); //?
                    processAuctionMessage(message);
                }
            } catch (IOException | ClassNotFoundException  e) {
                e.printStackTrace();
            }
        }

        /**
         * process a given auction message.  Uses the AMType to sort out what
         *     the message means and what the agent needs to do.
         * @param message
         */
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
                            bidStatus = "OUTBID on item " + item.name();
                            break;
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
                            bidStatus = "WINNER of item "+ item.name();
                        }
                        if (currentlyBidding.isEmpty()) {
                            activeBid = false;
                            break;
                        }
                    }
                    break;
                }
                case REJECTION: {
                    bidStatus = "REJECTED bid on "+ attemptedBid.name();
                    if (currentlyBidding.isEmpty()) {
                        activeBid = false;
                    }
                    break;
                }
                case ACCEPTANCE: {
                    bidStatus = "Bid accepted on " + attemptedBid.name();
                    currentlyBidding.add(attemptedBid);
                    break;
                }
                case DEREGISTER: {
                }
            }
        }
    }
}
