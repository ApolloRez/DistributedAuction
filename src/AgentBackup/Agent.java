package AgentBackup;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Steven Chase
 * This class holds the logic/communication of the Agent.
 * The Agent can communicate with the bank and  AuctionHouses.
 * The Agent can bid on an Item for sale at an AuctionHouse.
 * Each Agent creates the following objects based on private classes:
 * AgentIn: This active object is dedicated to receiving/processing
 * incoming messages from the bank. AuctionProxy(i) are created when AgentIn
 * receives the list of currently connected AuctionHouses.
 * AuctionProxy(i): Each AuctionProxy are active objects dedicated to receiving,
 * processing, and responding to ONE AuctionHouse socket.
 * Please Note: Due to the reason this class was made. This class is very
 * similar to AuctionHouse in the AuctionHouse package since I used it as a
 * foundation when writing this class.
 */
public class Agent {
    /**
     * bankSocket: socket used for Bank communication
     * input: input stream of bankSocket
     * out: output stream of bankSocket
     * log: records notifications of activity(bid,outbid,rejection,etc.) from
     *      AuctionHouse(s)
     * activeAuctions: list of AuctionHouses Agent is connected to
     * check:used for communication between Agent and uiUpdater in AgentGui
     *agentId: UUID used for identification. provided by Bank
     * shortId: first 4 letter String of agentID. Used for display purposes
     * bankBalance: amount of "money" the Agent can spend in auction
     * reservedBalance: amount of "money" being used in an auction
     * run: boolean to stop Threads after they do their current task
     */
    private Socket bankSocket;
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private ArrayList<String> log = new ArrayList<>();
    private List<AuctionProxy> activeAuctions = new ArrayList<>();
    private BlockingQueue<Boolean> check = new LinkedBlockingDeque<>();
    private UUID agentId;
    private String shortId;
    private double bankBalance;
    private double reservedBalance;
    private boolean run = true;

    /**
     * Constructor creates agent and attempts to create a Socket. If the Socket
     * fails to connect the process is aborted leaving an empty Agent. If the
     * Socket connects, it then sends a message to the bank to register and
     * creates the Thread dedicated to receiving Bank messages.
     * @param address ip address of Bank
     * @param port port number of Bank
     */
    public Agent(String address, int port){
        try{
            log.add("Connecting to bank");
            bankSocket = new Socket(address, port);
            log.add("connection successful");
            out = new ObjectOutputStream(bankSocket.getOutputStream());
            Message register = new Message.Builder()
                    .command(Message.Command.REGISTER_CLIENT).send(null);
            sendToBank(register);
            reservedBalance = 0.0;
            Thread inThread = new Thread(new AgentIn());
            inThread.start();
        } catch(IOException|NullPointerException u){
            check.add(false);
        }
    }

    /**
     * Sends the given Message to the Bank.
     * @param message The Message to send to the Bank via out.
     */
    private synchronized void sendToBank(Message message) {
        try{
            Message.Command temp = message.getCommand();
            out.reset();
            out.writeObject(message);
            if(temp == Message.Command.DEREGISTER_CLIENT){
                check.put(Boolean.TRUE);
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This Thread is dedicated to receiving/processing Messages from the Bank.
     */
    private class AgentIn implements Runnable {
        /**
         * If AgentIn isn't processing/handling messages. It's waiting
         * for a new message forever (until an Exception is thrown).
         */
        @Override
        public void run() {
            try {
                input = new ObjectInputStream(bankSocket.getInputStream());
                while(run){
                    Message message = (Message) input.readObject();
                    processMessage(message);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e){
                run = false;
            }
        }

        /**
         * This method is just a switch to direct the received Message
         * where it'll be processed accordingly.
         * @param message message received/passed along.
         */
        private void processMessage(Message message){
            Message.Command type = message.getCommand();
            switch(type){
                case GET_NET_INFO:
                    auctionList(message);
                    break;
                case TRANSFER:
                    break;
                case REGISTER_CLIENT:
                    registered(message);
                    break;
                case DEPOSIT:
                    deposited(message);
                    break;
                case GET_AVAILABLE:
                    available(message);
                    break;
                case GET_RESERVED:
                    reserve(message);
                    break;
            }
        }

        /**
         * If the message was related to getting available spending balance.
         * This method updates bankBalance with the amount provided by the
         * bank.
         * @param message Message with amount of available funds.
         */
        private void available(Message message){
            bankBalance = message.getAmount();
        }

        /**
         * If message was related to getting the amount of "money" in auction.
         * This method updates reservedBalance with the amount provided in the
         * Message
         * @param message Message with the amount of money in Auction/bidding.
         */
        private void reserve(Message message){
            reservedBalance = message.getAmount();
        }

        /**
         * When the Agent deposits money into the bank, and when the bank
         * responds. This methods sets bankBalance with the confirmed money
         * available.
         * @param message message with amount of money.
         */
        private void deposited(Message message){
            bankBalance = message.getAmount();
        }

        /**
         * When the Agent sends a registration Message and the Bank responds
         * with SUCCESS. This method is for setting the id the Bank provided
         * and send a DEPOSIT Message for 200. It then requests the information
         * of currently connected Banks. It then creates a Thread dedicated
         * to periodically getting the new balances from the bank. Finally, it
         * adds a TRUE boolean into check so the uiUpdater in AgentGui knows
         * it successfully registered.
         * @param message Message with Bank response to a REGISTER message.
         */
        private void registered(Message message){
            Message.Response response = message.getResponse();
            if(response == Message.Response.SUCCESS){
                agentId = message.getAccountId();
                String temp = agentId.toString();
                String fourDigit = "";
                for(int i = 0; i < 4;i++){
                    fourDigit = fourDigit.concat(String.valueOf(temp.charAt(i)));
                }
                shortId = fourDigit;
                //deposit funds;
                Message deposit = new Message.Builder()
                        .command(Message.Command.DEPOSIT)
                        .amount(200.0).send(agentId);
                sendToBank(deposit);
                getNetInfo();
                check.add(Boolean.TRUE);
                Thread thread = new Thread(new updater());
                thread.setDaemon(true);
                thread.start();
            }else{
                run = false;
                check.add(Boolean.FALSE);
            }
        }

        /**
         * When the Bank responds with a List of currently connected
         * AuctionHouses. It goes through the List and creates an AuctionProxy
         * for each AuctionHouse to handle communication. It also
         * sends a register AuctionMessage to each AuctionHouse.
         * @param message Message with list of available AuctionHouses
         */
        private void auctionList(Message message){
            List<NetInfo> info = message.getNetInfo();
            int size = info.size();
            for(int i = 0; i < size; i++){
                NetInfo auctionInfo = info.get(i);
                if(!isAlreadyActive(auctionInfo)){
                    AuctionProxy temp = new AuctionProxy(auctionInfo);
                    activeAuctions.add(temp);
                    Thread thread = new Thread(temp);
                    thread.start();
                    AuctionMessage register = AuctionMessage.Builder.newB()
                            .type(AuctionMessage.AMType.REGISTER)
                            .id(agentId)
                            .build();
                    temp.sendOut(register);
                }
            }
        }

    }

    /**
     *This Inner Class is dedicated to receiving, processing, and responding
     * to an AuctionHouse.
     */
    public class AuctionProxy implements Runnable{
        private Socket socket;//socket connecting to an AuctionHouse
        private ObjectInputStream auctionIn;//Input stream of socket
        private ObjectOutputStream auctionOut;//output stream of socket
        private AuctionMessage message = null;//last AuctionMessage from auction
        private ArrayList<Item> catalogue;//list of items for sale at auction
        private NetInfo info;//ip/port info of the AuctionHouse
        private UUID auctionId;//UUID of the AuctionHouse

        /**
         * The Constructor for AuctionProxy given a NetInfo(the ip and port of
         * an AuctionHouse) the constructor creates a socket to connect to
         * the bank.
         * @param info the ip and port number of an AuctionHouse
         */
        public AuctionProxy(NetInfo info){
            this.info = info;
            String ip = info.getIp();
            int port = info.getPort();
            try{
                socket = new Socket(ip,port);
                auctionOut = new ObjectOutputStream(socket.getOutputStream());
                auctionIn = new ObjectInputStream(socket.getInputStream());
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        /**
         * This method is dedicated to receiving and processing AuctionMessages
         * from an AuctionHouse.
         */
        @Override
        public void run() {
            do{
                try{
                    message = (AuctionMessage) auctionIn.readObject();
                    process(message);
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    message = null;
                }
            }while(message != null);
        }

        /**
         * This method is just a switch to redirect the AuctionMessage to the
         * appropriate method based on AMType(topic subject)
         * @param message Message being redirected
         */
        private void process(AuctionMessage message){
            AuctionMessage.AMType type = message.getType();
            switch(type){
                case BID:
                    break;
                case REGISTER:
                    registered(message);
                    break;
                case UPDATE:
                    update(message);
                    break;
                case ACCEPTANCE:
                    acceptance(message);
                    break;
                case WINNER:
                    winner(message);
                    break;
                case DEREGISTER:
                    agentShutdown(false);
                    break;
                case OUTBID:
                    outbid(message);
                    break;
                case REJECTION:
                    rejection(message);
                    break;
            }
        }

        /**
         * AuctionMessage was a notification for a rejection of a bid, so it
         * adds it to log to let the user know which bid for which Item failed.
         * @param message Message with rejection notification
         */
        private void rejection(AuctionMessage message){
            String name = message.getName();
            log.add("Your bid for " +name+" was unsuccessful");
        }

        /**
         * AuctionMessage was an outbid notification, so the method adds the
         * notification to the log to let the user know.
         * @param message message with outbid notification.
         */
        private void outbid(AuctionMessage message){
            UUID item = message.getItem();
            log.add("Your bid on "+ item+" was outbidded");
        }

        /**
         *This method shuts down the AuctionProxy in two ways. The first way
         * is sending one last message to AuctionHouse to deregister it,adds the
         * notification to log, and then it closes its streams and socket.
         * The second method closes the streams/sockets if they weren't already
         * closed. The first method is when we shutdown the AuctionProxy on
         * purpose. The second method is for error handling.
         * @param reason true for purposeful shutdown. False for error handling
         */
        private void agentShutdown(boolean reason){
            try{
                message = null;
                activeAuctions.remove(this);
                if(reason){
                    AuctionMessage shutdown = AuctionMessage.Builder.newB()
                            .type(AuctionMessage.AMType.DEREGISTER)
                            .build();
                    sendOut(shutdown);
                    log.add("Connection to Auction: " +auctionId+" closed");
                    if(!socket.isClosed()){
                        auctionOut.close();
                        auctionIn.close();
                        socket.close();
                    }
                }else{
                    //log.add("Connection to "+auctionId+" lost");
                    if(!socket.isClosed()){
                        auctionOut.close();
                        auctionIn.close();
                        socket.close();
                    }
                }
            }catch (IOException ignored){
            }
        }

        /**
         * When the AuctionHouse sends a message indicating the agent won an
         * Item. This method adds to the log which item the Agent won.
         * @param message message with the winner notification.
         */
        private void winner(AuctionMessage message){
            String name = message.getName();
            log.add("You won: "+name+". Congratulations!");
            Message transfer = new Message.Builder()
                    .command(Message.Command.TRANSFER)
                    .amount(message.getAmount())
                    .accountId(auctionId)
                    .send(agentId);
            sendToBank(transfer);
        }

        /**
         * When the Agent successfully bids on an item. This method lets the
         * user know via log entry their bid for item was successful.
         * @param message AuctionMessage with specific item the Agent
         *                successfully bid on
         */
        private void acceptance(AuctionMessage message){
            String name = message.getName();
            catalogue = message.getCatalogue();
            log.add("Your bid for "+ name+" was successful");
        }

        /**
         * When the AuctionHouse sends an AuctionMessage with a list of
         * Items currently for sale. This method updates the AuctionProxy's
         * catalogue with the one provided.
         * @param message Message with updated catalogue.
         */
        private void update(AuctionMessage message){
            catalogue = message.getCatalogue();
        }

         /**
         * When an Agent successfully registers with an AuctionHouse. The
         * AuctionHouse reponds with its UUID and catalogue. This method sets
         * the auctionId and catalogue from the AuctionMessage.
         * @param message message with the AuctionHouse UUID and catalogue.
         */
        private void registered(AuctionMessage message){
            auctionId = message.getId();
            catalogue = message.getCatalogue();
        }

        /**
         * When AgentGui processes a bid on an item. It calls this method to
         * send the BID AuctionMessage to the AuctionHouse with the needed info
         * @param amount The amount the User wants to bid
         * @param itemId The UUID of the Item the user wants to bid on
         * @param name The name of the Item hte user wants to bid on
         */
        public void bid(double amount,UUID itemId,String name){
            AuctionMessage bid = AuctionMessage.Builder.newB()
                    .type(AuctionMessage.AMType.BID)
                    .id(agentId)
                    .item(itemId)
                    .amount(amount)
                    .build();
            log.add("You bid: "+amount+" For: " +name);
            sendOut(bid);
        }

        /**
         * Method to send any AuctionMessage to the AuctionHouse the
         * AuctionProxy is assigned to.
         * @param message The AuctionMessage to send an AuctionHouse
         */
        private void sendOut(AuctionMessage message){
            try{
                auctionOut.reset();
                auctionOut.writeObject(message);
            }catch(IOException ignored){
            }
        }

        /**
         * Gets the AuctionId of AuctionProxy and grabs the first four letters
         * of the UUID. This method is to help with displaying the auctionId.
         * @return returns a String of the first 4 letters of the UUID.
         */
        public String getShortAuctionId(){
            String id = auctionId.toString();
            String shortened = "";
            for(int i = 0; i < 4;i++){
                shortened = shortened.concat(String.valueOf(id.charAt(i)));
            }
            return shortened;
        }

        /**
         * Method used when you want access to the catalogue of an AuctionProxy
         * @return Returns the AuctionProxy's catalogue variable.
         */
        public ArrayList<Item> getCatalogue(){
            return catalogue;
        }

        /**
         * This method checks if the AuctionProxy's NetInfo(ip/port) is the
         * same as the NetInfo given to the method.
         * @param info NetInfo being compared
         * @return true if the NetInfos are the same, false otherwise.
         */
        public boolean equals(NetInfo info){
            boolean ip = info.getIp().equals(this.info.getIp());
            int port = Integer.compare(info.getPort(),this.info.getPort());
            return ip && port== 0;
        }
    }

    /**
     * This inner class/Thread is dedicated to updating the amount of money
     * available and at Auction from the Bank. The Thread also sends UPDATE
     * AuctionMessges to AuctionHouses to get latest info regarding what's for
     * sale.
     */
    private class updater implements Runnable{
        @Override
        public void run() {
            while(run){
                try{
                    Message available = new Message.Builder()
                            .command(Message.Command.GET_AVAILABLE)
                            .send(agentId);
                    sendToBank(available);
                    Message reserve = new Message.Builder()
                            .command(Message.Command.GET_RESERVED)
                            .send(agentId);
                    sendToBank(reserve);
                    for(int i = 0; i < activeAuctions.size(); i++){
                        AuctionMessage catalogue = AuctionMessage.Builder.newB()
                                .type(AuctionMessage.AMType.UPDATE)
                                .id(agentId)
                                .build();
                        activeAuctions.get(i).sendOut(catalogue);
                    }
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the log used for activity
     * @return returns ArrayList<> log
     */
    public ArrayList<String> getLog(){
        return log;
    }

    /**
     * Call this method if you want the activeAuctions variable
     * @return returns Agent's activeAuctions
     */
    public List<AuctionProxy> getActiveAuctions(){
        return activeAuctions;
    }

    /**
     * Gets the Agent's available amount to spend.
     * @return returns Agent's bankBalance
     */
    public double getBankBalance(){
        return bankBalance;
    }

    /**
     * This method gets the Agent's amount of "money" in an auction
     * @return returns Agent's reservedBalance
     */
    public double getReservedBalance(){
        return reservedBalance;
    }

    /**
     * This method gets the Agent's 4 letter version of Agent's agentId.
     * @return returns Agent's shortId.
     */
    String getShortId(){
        return shortId;
    }

    /**
     * This method sends a Message to Bank requesting list of currently
     * connected AuctionHouses.
     */
    public void getNetInfo(){
        Message getAuctions = new Message.Builder()
                .command(Message.Command.GET_NET_INFO).send(agentId);
        sendToBank(getAuctions);
    }

    /**
     * Method for communication between Agent and AgentGui. It a true boolean
     * to check so the uiUpdater Thread can check if the Agent successfully
     * registered.
     * @return true if Agent registered with bank, false otherwise.
     */
    public boolean checkRegistration(){
        try{
            Boolean temp = check.take();
            return temp;
        }catch(InterruptedException ignored){}
        return false;
    }

    /**
     * This method shuts down all Threads and closes all streams/sockets.
     */
    public void shutdown(){
        try{
            run = false;
            Message deregister = new Message.Builder()
                    .command(Message.Command.DEREGISTER_CLIENT)
                    .send(agentId);
            sendToBank(deregister);
            try{
                Boolean check = this.check.take();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            input.close();
            out.close();
            bankSocket.close();
            while(activeAuctions.size() != 0){
                activeAuctions.get(0).agentShutdown(true);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Method to check any AuctionProxys in activeAuctions have connected
     * to an AuctionHouse with the NetInfo provided. This method is to make
     * sure the Agent doesn't connect to an AuctionHouse twice.
     * @param info NetInfo being check if it has a corresponding AuctionProxy
     * @return returns true if an AuctionProxy has the same NetInfo as the
     * one provided, false otherwise.
     */
    public boolean isAlreadyActive(NetInfo info){
        for( AuctionProxy auction:activeAuctions){
            if(auction.equals(info)){
                return true;
            }
        }
        return false;
    }
}
