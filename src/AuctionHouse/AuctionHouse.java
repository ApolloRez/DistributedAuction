package AuctionHouse;

import shared.AuctionMessage;
import shared.AuctionMessage.AMType;
import shared.Message;
import shared.Message.Command;
import shared.NetInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven Chase
 * This class holds the logic/communication of the Auction House.
 * The Auction House can communicate with the bank and an agent.
 * Each AuctionHouse creates the following objects based on private classes:
 * AuctionServer:This active object accepts socket requests and creates
 * an active object AgentProxy for that socket.
 * AuctionIn: This active object is dedicated to receiving/processing
 * incoming messages from the bank.
 * AgentProxy(i): These active objects are dedicated two receiving,
 * processing, and responding to ONE agent socket.
 * Countdown: This active object is dedicated to periodically updating
 * time elasped since an auction item has been on sale. It also
 * removes any item's auction has ended(30 seconds since
 * last bid/creation.
 * last bid/creation.
 */
public class AuctionHouse{
    private ServerSocket server; //ServerSocket for agent communication
    private Socket auctionClient; //Socket for bank communication
    private ObjectInputStream input;//stream to receive messages to Auction
    private ObjectOutputStream out; //output stream to send messages
    private ItemList list; //List of possible item names
    private ArrayList<Item> catalogue = new ArrayList<>();
    //list of items currently for sale
    private List<AgentProxy> activeAgents = new LinkedList<>();
    //List of currently connected agents.
    private BlockingQueue<Boolean> check = new LinkedBlockingDeque<>();
    //Blocking queue used for AuctionGui communication
    private ArrayList<String> log; //log of activities/notifications
    private double balance = 0.0; // the bank balance of the AuctionHouse
    private boolean run = true; //boolean to keep certain threads looping
    private UUID auctionId; //The id given to the AuctionHouse by the bank
    private String ip;  //ip address of Auction server
    private int port; //port number of the Auction server

    /**
     * The constructor first connects to the bank and starts its own server.
     * The AuctionHouse object then immediately registers with the bank
     * then creates a thread to handle socket join requests to the server.
     * @param address the ip address of the bank
     * @param clientPort the port number of the bank
     * @param serverPort port number for Auction to create server
     */
    public AuctionHouse(String address, int clientPort, int serverPort){
        setupItemList();
        log = new ArrayList<>();
        try{
            log.add("Connecting to bank");
            auctionClient = new Socket();
            auctionClient.connect( new InetSocketAddress(address,clientPort),2000);
            server = new ServerSocket(serverPort);
            Thread serverThread = new Thread(new AuctionServer());
            serverThread.start();
            out = new ObjectOutputStream(auctionClient.getOutputStream());
            setupItemList();
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
            }
            port = server.getLocalPort();
            NetInfo serverInfo = new NetInfo(ip,port);
            List<NetInfo> ahInfo = new LinkedList<>();
            ahInfo.add(serverInfo);
            Message register = new Message.Builder().command(Command.REGISTER_AH)
                    .netInfo(ahInfo).send(null);
            sendToBank(register);
            Thread inThread = new Thread(new AuctionIn());
            inThread.start();
            } catch(IOException u){
            u.printStackTrace();
            check.add(false);
        }
    }

    /**
     * creates items for the auction house from the item list
     * @param needed number of items to create
     */
    private void addItems(int needed){
        while(needed > 0){
            String name = list.getRandomName();
            int random = new Random().nextInt(50);
            Item item = new Item(name, random,auctionId);
            catalogue.add(item);
            needed--;
        }
    }

    /**
     * creates catalogue for items to sell
     */
    private void setupItemList(){
        int test = new Random().nextInt(20);
        list = ItemList.createNameList("Destiny2.txt");
    }

    /**
     * This class is dedicated to handling messages sent from an specific agent.
     */
    private class AuctionIn implements Runnable {
        /**
         * loop to wait for new messages forever(until an exception is thrown).
         * Incoming messages that aren't looped (GET_AVAILABLE) are added
         * to the log.
         */
        @Override
        public void run() {
            try {
                input = new ObjectInputStream(auctionClient.getInputStream());
                while(run){
                    Message message = (Message) input.readObject();
                    Command temp = message.getCommand();
                    if(temp != Command.GET_AVAILABLE){
                        log.add("Bank: " + message);
                    }
                    processMessage(message);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e){
                try {
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void processMessage(Message message){
            Command type = message.getCommand();
            switch(type){
                case HOLD:
                    hold(message);
                    break;
                case RELEASE_HOLD:
                    released(message);
                    break;
                case REGISTER_AH:
                    registered(message);
                    break;
                case GET_AVAILABLE:
                    bankBalance(message);
                    break;
            }
        }

        private void hold(Message message) {
            UUID bidder = message.getAccountId();
            Message.Response response = message.getResponse();
            AgentProxy temp = agentSearch(bidder);
            if (temp != null) {
                if (response == Message.Response.SUCCESS) {
                    try{
                        temp.bankSignOff.put(true);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }else if(response == Message.Response.INSUFFICIENT_FUNDS){
                    try{
                        temp.bankSignOff.put(false);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * updates the balance variable to the bank balance given by the bank,
         *
         * @param message the message with the available balance for this object
         */
        private void bankBalance(Message message) {
            balance = message.getAmount();
        }

        private void released(Message message){
            Message.Response response = message.getResponse();
            if(response == Message.Response.SUCCESS){
                log.add("release was successful");
            }else{
                log.add("release failed");
            }
        }
        private void registered(Message message){
            auctionId = message.getAccountId();
            addItems(4);
            check.add(true);
            Thread timer = new Thread(new Countdown());
            timer.setDaemon(true);
            timer.setPriority(4);
            timer.start();
        }
    }
    /**
     * This inner class is dedicated to creating AuctionProxys for each
     * socket join request.
     */
    private class AuctionServer implements Runnable{
        @Override
        public void run() {
            try{
                while(run){
                    Socket clientSocket = server.accept();
                    AgentProxy newAgent = new AgentProxy(clientSocket);
                    activeAgents.add(newAgent);
                    Thread client = new Thread(newAgent);
                    client.start();
                }
            }catch (IOException e){
                run = false;
            }
        }
    }

    /**
     * This class is dedicated to communicating/processing messages from/with
     * agents.
     * Each instance of this class represents communication with one agent.
     */
    private class AgentProxy implements Runnable {
        private Socket agentSocket; //the socket connected to an agent
        private ObjectInputStream agentIn; //Input stream for agentSocket
        private ObjectOutputStream agentOut;//output stream for agentSocket
        private UUID agentId; //The UUID of the agent when it registers
        private AuctionMessage message = null; //The message read from agentIn
        private BlockingQueue<Boolean> bankSignOff;
        //Blocking queue for waiting on a HOLD response from the bank

        /**
         * Constructor for an AgentProxy object. The constructor takes in
         * an accepted socket from the server variable, opens streams from it,
         * and begins communication.
         * @param socket the accepted socket from the server variable
         */
        public AgentProxy(Socket socket){
            this.agentSocket = socket;
            bankSignOff = new LinkedBlockingDeque<>();
            try{
                agentIn = new ObjectInputStream(agentSocket.getInputStream());
                agentOut = new ObjectOutputStream(
                        agentSocket.getOutputStream());
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        /**
         * The run method is dedicating to reading message from an agent.
         * The method also adds the incoming message to the log
         */
        @Override
        public void run() {
            do{
                try{
                    message = (AuctionMessage) agentIn.readObject();
                    AMType type = message.getType();
                    if(type != AMType.UPDATE){
                        log.add("From a client: " + message);
                    }
                    process(message);
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    agentShutdown(false);
                    message = null;
                }
            }while(message != null && run);
        }

        /**
         *This method either gracefully or forcefully closes the AgentProxy's
         * socket, streams, and threads.A graceful shutdown means messaging
         * agents to let them know the auction house is shutting down. A
         * forceful shutdown means either to shutdown from a DEREGISTER message
         * or shutdown due to Exceptions
         * @param reason True if a graceful shutdown, false if for
         *               error handling/DEREGISTER message
         */
        private void agentShutdown(Boolean reason){
            try{
                message = null;
                if(reason){
                    AuctionMessage shutdown = AuctionMessage.Builder.newB()
                            .type(AuctionMessage.AMType.DEREGISTER)
                            .build();
                    sendOut(shutdown);
                    log.add("Connection to Agent " +agentId+" closed");
                    if(!agentSocket.isClosed()){
                        agentOut.close();
                        agentIn.close();
                        agentSocket.close();
                    }
                    activeAgents.remove(this);
                }else{
                    if(!agentSocket.isClosed()){
                        agentOut.close();
                        agentIn.close();
                        agentSocket.close();
                    }
                    activeAgents.remove(this);
                }
            }catch (IOException ignored){
            }
        }

        /**
         * This method is a switch to redirect incoming messages from agents
         * to the appropriate method for processing.
         * @param message The message sent from an agent
         */
        private void process(AuctionMessage message){
            AMType type = message.getType();
            switch(type){
                case BID:
                    bid(message);
                    break;
                case REGISTER:
                    register(message);
                    break;
                case UPDATE:
                    update();
                    break;
                case DEREGISTER:
                    agentShutdown(false);
                    break;
            }
        }

        /**
         * This method creates the message with the updated catalogue
         * and passes it to sendOut to send to the agent
         */
        private void update(){
            AuctionMessage update = AuctionMessage.Builder.newB()
                    .type(AMType.UPDATE)
                    .id(auctionId)
                    .list(catalogue)
                    .build();
            sendOut(update);
        }

        /**
         * This method is letting the connected agent know they successfully
         * registered and sends the catalogue of items for sale. The method
         * also stores the agent's UUID for future reference
         * @param message The register message the agent sent
         */
        private void register(AuctionMessage message){
            agentId = message.getId();
            AuctionMessage reply =AuctionMessage.Builder.newB()
            .type(AMType.REGISTER).id(auctionId).list(catalogue).build();
            sendOut(reply);
        }

        /**
         * This replaces the new bidder/amount with the old bidder/amount and
         * lets the old bidder know they were outbided
         * @param oldBidder UUID of the last bidder
         * @param item The item that has a new bidder
         */
        private void outBid(UUID oldBidder,Item item){
            AgentProxy agent = agentSearch(oldBidder);
            AuctionMessage outbid = AuctionMessage.Builder.newB()
                    .type(AMType.OUTBID)
                    .item(item.getItemID())
                    .name(item.name())
                    .id(agentId)
                    .build();
            assert agent != null;
            agent.sendOut(outbid);
        }

        /**
         * The item's auction has ended and there's a bidder. This method
         * lets the bidder know they won.
         * @param item The item the bidder won
         */
        private void winner(Item item){
            AuctionMessage winner = AuctionMessage.Builder.newB()
                    .type(AMType.WINNER)
                    .id(agentId)
                    .item(item.getItemID())
                    .name(item.name())
                    .amount(item.getCurrentBid())
                    .build();
            sendOut(winner);
        }

        /**
         * This method grabs the itemID, bidderId, name, and amount of the
         * item to bid on. First it checks if the item is still for sale, then
         * checks if the bid amount is above the minimum/current bid. Then it
         * requests the bank to hold the bidded funds and waits for a response.
         * After receiving the response, it then decides whether to reject or
         * accept the bid.
         * @param message The message with AMType BID
         */
        private void bid(AuctionMessage message){
            UUID itemID = message.getItem();
            UUID bidderId = message.getId();
            String name = message.getName();
            double amount = message.getAmount();
            Item bidItem = itemSearch(itemID);
            if(bidItem == null){
                reject(itemID,name);
                return;
            }
            double value = bidItem.getCurrentBid();
            if( value < bidItem.getMinimumBid()){
                value = bidItem.getMinimumBid();
            }
            if(amount > value){
                Message requestHold = new Message.Builder()
                        .command(Command.HOLD)
                        .accountId(bidderId).amount(amount).send(this.agentId);
                try{
                    //requests the hold.
                    sendToBank(requestHold);
                    //waits for the response from bank.
                    Boolean success = bankSignOff.take();
                    if(success){
                        //accepts bid and lets the last bidder know
                        //they were outbidded
                        UUID oldBidder = bidItem.getBidder();
                        if(oldBidder != null){
                            release(oldBidder, value);
                            outBid(oldBidder, bidItem);
                        }
                        bidItem.outBid(bidderId, amount);
                        accept(bidItem.getItemID(), bidItem.name());
                    }else{
                        reject(itemID,name);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }else{
                reject(itemID,name);
            }
        }

        /**
         * requests the bank to release the hold of (amount) amount on account
         * id
         * @param id the account(bidder) having their funds released
         * @param amount the amount requested to release
         */
        private synchronized void release(UUID id, Double amount){
            Message release = new Message.Builder()
                    .command(Command.RELEASE_HOLD)
                    .accountId(id).amount(amount).send(auctionId);
            sendToBank(release);
        }

        /**
         * Lets the bidder know its bid was rejected due to various reasons
         * (not enough funds, bid not high enough, etc.)
         * @param itemID The UUID of the item bid on
         * @param name the name of the item
         */
        private void reject(UUID itemID, String name){
            AuctionMessage reject = AuctionMessage.Builder.newB().
                    type(AMType.REJECTION)
                    .name(name)
                    .item(itemID)
                    .build();
            sendOut(reject);
        }

        /**
         * Once a bid by an Agent is accepted, this method lets the agent
         * know their bid was accepted. The message also contains the updated
         * catalogue
         * @param item UUID of the item bid on
         * @param name String/name of the item bid on
         */
        private void accept(UUID item, String name){
            AuctionMessage accept = AuctionMessage.Builder.newB()
                    .type(AMType.ACCEPTANCE)
                    .id(item)
                    .name(name)
                    .list(catalogue)
                    .build();
            sendOut(accept);
        }

        /**
         * This method is given an AuctionMessage and writes/sends it to
         * agentSocket. The method add the sent message to the log.
         * @param message the message being sent
         */
        private void sendOut(AuctionMessage message){
            try{
                if(message.getType() != AMType.UPDATE){
                    log.add("To Agent: " + message);
                }
                agentOut.reset();
                agentOut.writeObject(message);
            }catch(IOException e){
                agentShutdown(false);
            }
        }
    }

    /**
     * This class is dedicated to updating the seconds left for items on
     * sale (i.e. updating timeLeft in the Items in catalogue).
     */
    private class Countdown implements Runnable{
        @Override
        public void run() {
            while(run){
                try{
                    int needed =  4 - catalogue.size();
                    if(needed > 0){
                        addItems(needed);
                    }
                    for(int i = 0;i < catalogue.size(); i++){
                        long currentTime = System.currentTimeMillis();
                        Item item = catalogue.get(i);
                        item.updateTimer(currentTime);
                        long timeLeft = item.getTimeLeft();
                        if(timeLeft <= 0){
                            itemResult(item);
                        }
                    }
                    Thread.sleep(200);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * After the time expires on an Item for sale. This method checks if
     * there was any bidders and sends the WINNER message to that bidder.
     * Also releases the hold on the bidders amount.
     * @param item the item being checked
     */
    private void itemResult(Item item){
        UUID bidder = item.getBidder();
        AgentProxy agent = agentSearch(bidder);
        if (agent != null) {
            agent.winner(item);
            Message release = new Message.Builder()
                    .command(Command.RELEASE_HOLD)
                    .amount(item.getCurrentBid())
                    .accountId(bidder).send(auctionId);
            sendToBank(release);
        }
        catalogue.remove(item);
    }

    /**
     * Sends the given message to the bank and adds it to the log for display.
     * Looped messages (GET_AVAILABLE) are ignored when adding to log.
     * @param message message being sent to the bank.
     */
    private synchronized void sendToBank(Message message) {
        try {
            Command temp = message.getCommand();
            if(temp != Command.GET_AVAILABLE){
                log.add("Bank: " + message);
            }
            out.reset();
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * searches for agent in active agent list
     * @param id id of agent we want
     * @return returns the agentProxy we want, null otherwise
     */
    private AgentProxy agentSearch(UUID id){
        for(AgentProxy agent: activeAgents){
            if(agent.agentId.equals(id)){
                return agent;
            }
        }
        return null;
    }

    /**
     * searches the catalogue for an item based on UUID
     * @param id the UUID of the item being searched
     * @return returns the item searched, or null if item isn't found
     */
    private Item itemSearch(UUID id){
        for(Item item: catalogue){
            if(item.getItemID().equals(id)){
                return item;
            }
        }
        return null;
    }

    /**
     * This method is used for AuctionGui/Auction communication. The UiUpdater
     * Thread  in AuctionGui checks if the Auction object was able to connect
     * and register to the bank.
     * @return returns true if Auction connected and registered with the bank.
     * Returns false otherwise
     */
    public boolean checkRegistration(){
        try{
            return check.take();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method closes all sockets and streams. It then signals all threads
     * to stop after finishing their current task
     * (by making them throw exceptions)
     */
    public void shutdown(){
        try{
            run = false;
            NetInfo serverInfo = new NetInfo(ip,port);
            List<NetInfo> ahInfo = new LinkedList<>();
            ahInfo.add(serverInfo);
            Message deregister = new Message.Builder()
                    .command(Command.DEREGISTER_AH)
                    .netInfo(ahInfo).send(auctionId);
            sendToBank(deregister);
            out.close();
            input.close();
            server.close();
            while(!activeAgents.isEmpty()){
                activeAgents.get(0).message = null;
                activeAgents.get(0).agentShutdown(true);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void getBankBalance(){
        Message getAvailable = new Message
                .Builder()
                .command(Command.GET_AVAILABLE)
                .send(auctionId);
        sendToBank(getAvailable);
    }

    /**
     * @return returns the catalogue
     */
    public ArrayList<Item> getCatalogue(){
        return catalogue;
    }

    /**
     * @return returns the log ArrayList
     */
    public ArrayList<String> getLog(){
        return log;
    }

    /**
     * @return returns the balance parameter
     */
    public double getBalance(){
        return balance;
    }

    /**
     * Grabs the first 4 digits of a given UUID and returns a string
     * @param aId The UUID being turned into a 4 digit string
     * @return returns a 4 digit string of the given UUID
     */
    public String getShortId(UUID aId){
        String id = aId.toString();
        String shortened = "";
        for(int i = 0; i < 4;i++){
            shortened = shortened.concat(String.valueOf(id.charAt(i)));
        }
        return shortened;
    }

    /**
     * @return returns the auctionId parameter
     */
    UUID getAuctionId(){
        return auctionId;
    }
}
