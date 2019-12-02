package AuctionHouse;

import bank.service.ConnectionLoggerService;
import shared.AuctionMessage;
import shared.AuctionMessage.AMType;
import shared.Message;
import shared.NetInfo;
import shared.Message.Command;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class AuctionHouse{
    private ServerSocket server;
    private Socket auctionClient;
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private ItemList list;
    private ArrayList<Item> catalogue = new ArrayList<Item>();
    private Set<AgentProxy> activeAgents = new HashSet<>();
    private final ConnectionLoggerService log;
    private double balance = 0;
    private boolean run = true;
    private UUID auctionId;

    public AuctionHouse(String address, int clientPort, int serverPort){
        setupItemList();
        log = ConnectionLoggerService.getInstance();
            try{
                log.add("Connecting to bank");
                System.out.println();
                auctionClient = new Socket(address, clientPort);
                log.add("Connection established");
                server = new ServerSocket(serverPort);
                System.out.println("Action house server started");
                Thread serverThread = new Thread(new AuctionServer());
                serverThread.start();
                out = new ObjectOutputStream(auctionClient.getOutputStream());
                setupItemList();
                String ip;
                try(final DatagramSocket socket = new DatagramSocket()){
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    ip = socket.getLocalAddress().getHostAddress();
                }
                int port = server.getLocalPort();
                NetInfo serverInfo = new NetInfo(ip,port);
                List<NetInfo> ahInfo = new LinkedList<>();
                ahInfo.add(serverInfo);
                Message register = new Message.Builder().command(Command.REGISTER_AH)
                        .netInfo(ahInfo).send(null);
                sendToBank(register);

                Thread inThread = new Thread(new AuctionIn());
                inThread.start();

                //shutdown();
            } catch(IOException u){
                u.printStackTrace();
            }
    }

    public void shutdown(){
        try{
            run = false;
            String ip;
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
            }
            int port = server.getLocalPort();
            NetInfo serverInfo = new NetInfo(ip,port);
            List<NetInfo> ahInfo = new LinkedList<>();
            ahInfo.add(serverInfo);
            Message deregister = new Message.Builder().command(Command.DEREGISTER_AH)
                    .netInfo(ahInfo).send(auctionId);
            sendToBank(deregister);
            assert out != null;
            out.flush();
            input.close();
            out.close();
            server.close();
            for(AgentProxy agent:activeAgents){
                agent.shutdown();
            }

        }catch(IOException e){
            e.printStackTrace();
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
     * starts the auction server for agents to connect
     */
    private class AuctionServer implements Runnable{
        @Override
        public void run() {
            System.out.println("serverThread started");
            try{
                while(run){
                    Socket clientSocket = server.accept();
                    AgentProxy newAgent = new AgentProxy(clientSocket);
                    activeAgents.add(newAgent);
                    Thread client = new Thread(newAgent);
                    client.start();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e){
            }
        }
    }

    public static void main(String[] args) {
        int clientPort = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);
        AuctionHouse server = new AuctionHouse(args[0], clientPort, serverPort);
    }

    /**
     * holds agent id and socket for communication/bookeeping
     */
    private class AgentProxy implements Runnable {
        private Socket agentSocket;
        private ObjectInputStream agentIn;
        private ObjectOutputStream agentOut;
        private UUID agentID;
        private AuctionMessage message = null;
        private BlockingQueue<Boolean> bankSignoff= new LinkedBlockingDeque<>();
        @Override
        public void run() {
            do{
                try{
                    message = (AuctionMessage) agentIn.readObject();
                    process(message);
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    try {
                        agentIn.close();
                        agentOut.close();
                        agentSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }while(message != null);
        }
        private void process(AuctionMessage message){
            AMType type = message.getType();
            switch(type){
                case BID:
                    bid(message);
                    break;
                case REGISTER:
                    register(message);
                    break;
                default: System.out.println("uh oh");
            }
        }
        private void register(AuctionMessage message){
            agentID = message.getId();
            activeAgents.add(this);
            AuctionMessage reply =AuctionMessage.Builder.newB()
            .type(AMType.REGISTER).list(catalogue).build();
            sendOut(reply);
        }

        private void outBid(UUID oldBidder,UUID itemID){
            AgentProxy agent = agentSearch(oldBidder);
            AuctionMessage outbid = AuctionMessage.Builder.newB()
                    .type(AMType.OUTBID).item(itemID).id(agentID).build();
            assert agent != null;
            agent.sendOut(outbid);
        }

        private void winner(UUID bidder,UUID itemID){
            AuctionMessage winner = AuctionMessage.Builder.newB()
                    .type(AMType.WINNER).id(agentID).item(itemID).build();
            sendOut(winner);
        }


        private void bid(AuctionMessage message){
            UUID itemID = message.getItem();
            UUID bidderId = message.getId();
            double amount = message.getAmount();
            Item bidItem = itemSearch(itemID);
            if(bidItem == null){
                reject();
            }
            assert bidItem != null;
            double value = bidItem.getCurrentBid();
            if( value < bidItem.getMinimumBid()){
                value = bidItem.getMinimumBid();
            }
            if(amount > value){
                Message requestHold = new Message.Builder()
                        .command(Command.HOLD)
                        .accountId(bidderId).amount(value).send(this.agentID);

                try{
                    out.writeObject(requestHold);
                    Boolean success = bankSignoff.take();
                    if(success){
                        UUID oldBidder = bidItem.getBidder();
                        release(oldBidder,value);
                        outBid(oldBidder,bidItem.getItemID());
                        bidItem.outBid(bidderId,amount);
                        accept();
                    }else{
                       reject();
                    }
                }catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }
            }else{
                reject();
            }
        }

        private synchronized void release(UUID id, Double amount){
            Message release = new Message.Builder()
                    .command(Command.RELEASE_HOLD)
                    .accountId(id).amount(amount).send(auctionId);
            sendToBank(release);
        }
        private void reject(){
            AuctionMessage reject = AuctionMessage.Builder.newB().
                    type(AMType.REJECTION).build();
            sendOut(reject);
        }
        private void accept(){
            AuctionMessage accept = AuctionMessage.Builder.newB()
                                    .type(AMType.ACCEPTANCE).build();
            sendOut(accept);
        }

        private void sendOut(AuctionMessage message){
            try{
                agentOut.writeObject(message);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        private void shutdown() throws IOException {
            agentSocket.close();
        }

        public AgentProxy(Socket socket){
            this.agentSocket = socket;
            try{
                agentIn = new ObjectInputStream(agentSocket.getInputStream());
                agentOut = new ObjectOutputStream(
                                agentSocket.getOutputStream());
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private class Countdown implements Runnable{

        @Override
        public void run() {
            System.out.println("started timer");
            while(run){
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
                        System.out.println(timeLeft);
                        itemResult(item);
                    }
                }
            }
        }
    }

    private void itemResult(Item item){
        UUID bidder = item.getBidder();
        UUID itemID = item.getItemID();
        AgentProxy agent = agentSearch(bidder);
        if(agent != null){
            agent.winner(bidder,itemID);
        }
        catalogue.remove(item);
    }

    private synchronized void sendToBank(Message message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Input for the auction from bank
     */
    private class AuctionIn implements Runnable {

        @Override
        public void run() {
            System.out.println("clientIn thread started");
            try {
                input = new ObjectInputStream(auctionClient.getInputStream());
                while(run){
                    Message message = (Message) input.readObject();
                    System.out.println(message);
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
                default: System.out.println("uh oh");
            }
        }
        private void hold(Message message){
            UUID bidder = message.getAccountId();
            Message.Response response = message.getResponse();
            AgentProxy temp = agentSearch(bidder);
            if(temp != null){
                if(response == Message.Response.SUCCESS){
                    try{
                        temp.bankSignoff.put(true);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }else if(response == Message.Response.INSUFFICIENT_FUNDS){
                    try{
                        temp.bankSignoff.put(false);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }else{
                System.out.println("Not Good");
            }
        }

        private void released(Message message){
            Message.Response response = message.getResponse();
            if(response == Message.Response.SUCCESS){
                System.out.println("release was successful");
            }else{
                System.out.println("release failed");
            }
        }
        private void registered(Message message){
            auctionId = message.getAccountId();
            addItems(4);
            Thread timer = new Thread(new Countdown());
            timer.setDaemon(true);
            timer.setPriority(4);
            timer.start();
            System.out.println("bank registration successful:"+auctionId);
        }
    }

    /**
     * searches for agent in active agent list
     * @param id id of agent we want
     * @return returns the agentProxy we want, null otherwise
     */
    private AgentProxy agentSearch(UUID id){
        for(AgentProxy agent: activeAgents){
            if(agent.agentID == id){
                return agent;
            }
        }
        return null;
    }

    private Item itemSearch(UUID id){
        for(Item item: catalogue){
            if(item.getItemID() == id){
                return item;
            }
        }
        return null;
    }

    /**
     * @return returns the catalogue
     */
    public ArrayList<Item> getCatalogue(){
        return catalogue;
    }
}
