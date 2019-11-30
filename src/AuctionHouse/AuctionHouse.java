package AuctionHouse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import shared.BankMessage;
import shared.AuctionMessage;
import shared.AuctionMessage.AMType;

public class AuctionHouse{
    private ServerSocket server;
    private Socket auctionClient;
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private ItemList list;
    private ArrayList<Item> catalogue = new ArrayList<Item>();
    private Set<AgentProxy> activeAgents = new HashSet<>();
    private double balance = 0;
    private boolean run = true;

    private AuctionHouse(String address,int clientPort, int serverPort){
        setupItemList();
            try{
                auctionClient = new Socket(address, clientPort);
                server = new ServerSocket(serverPort);
                setupItemList();
                addItems(3);
                Thread serverThread = new Thread(new AuctionServer());
                serverThread.start();
                out = new ObjectOutputStream(auctionClient.getOutputStream());
                Thread inThread = new Thread(new AuctionIn());
                inThread.start();
            } catch(IOException u){
                u.printStackTrace();
            }
            /*String line = "";
            System.out.println("you should not be here yet");
            boolean done = false;
            while (!done) {
                try {
                    line = input.readLine();
                    if(line.equals("over")){
                        done = true;
                    }
                    assert out != null;
                    out.writeObject(line);
                }
                catch(IOException i) {
                    System.out.println(i);
                }
            }*/
    }

    private void shutdown(){
        try{
            assert out != null;
            out.flush();
            input.close();
            out.close();
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
        while(needed >= 0){
            String name = list.getRandomName();
            int random = new Random().nextInt(50);
            double value = random;
            Item item = new Item(name,value);
            catalogue.add(item);
            needed--;
        }
    }

    /**
     * creates catalogue for items to sell
     */
    private void setupItemList(){
        int test = new Random().nextInt(20);
        ItemList list = ItemList.createNameList("Destiny2.txt");
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
            }catch(IOException| InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Input for the auction from bank
     */
    private class AuctionIn implements  Runnable{
        private ObjectInputStream clientInput;
        @Override
        public void run() {
            System.out.println("clientIn thread started");
            try{
                clientInput = new ObjectInputStream(auctionClient.getInputStream());
                input  = new ObjectInputStream(auctionClient.getInputStream());
                BankMessage bankMessage = (BankMessage)input.readObject();
            }catch(IOException |ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * holds agent id and socket for communication/bookeeping
     */
    private class AgentProxy implements Runnable{
        private Socket agentSocket;
        private ObjectInputStream agentIn;
        private ObjectOutputStream agentOut;
        private UUID id;
        private AuctionMessage message = null;
        @Override
        public void run() {
            do{
                try{
                    message = (AuctionMessage) agentIn.readObject();
                    process(message);
                }catch(IOException|ClassNotFoundException e){
                    e.printStackTrace();
                }
            }while(message != null);
        }
        private void process(AuctionMessage message){
            AMType type = message.getType();
            switch(type){
                case BID:      bid(message);
                               break;
                case REGISTER: register(message);
                               break;
                default: System.out.println("uh oh");
            }
        }
        private void register(AuctionMessage message){
            id = message.getId();
            if(activeAgents.size() == 0){
                Thread countdown = new Thread(new Countdown());
                countdown.start();
            }
            activeAgents.add(this);
            AuctionMessage reply = AuctionMessage.Builder.newB()
            .type(AMType.ACCEPTANCE).list(catalogue).build();
            sendOut(reply);
        }

        private void outBid(){
            AuctionMessage outbid = AuctionMessage.Builder.newB()
                    .type(AMType.OUTBID).id(id).build();
            sendOut(outbid);
        }

        private void winner(Item item){
            AuctionMessage winner = AuctionMessage.Builder.newB()
                    .type(AMType.WINNER).id(id).item(item).build();
            sendOut(winner);
        }


        private void bid(AuctionMessage message){
            Item bidItem = message.getItem();
            UUID bidderId = message.getId();
            double amount = message.getAmount();
            if(amount > bidItem.value()){

            }else{
                reject();
            }
        }

        private void reject(){
            AuctionMessage reject = AuctionMessage.Builder.newB().
                    type(AMType.REJECTION).build();
            sendOut(reject);
        }

        private void holdFunds(){
            
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
        private List<Item> toRemove = new ArrayList<Item>();
        private boolean remove = false;
        @Override
        public void run() {
            while(run){
                for(Item item: catalogue){
                    item.decrement();
                    if(item.over()){
                        remove = true;
                        toRemove.add(item);
                    }
                }
                if(remove){
                    for(Item item:toRemove){
                        catalogue.remove(item);
                    }
                }
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void itemResult(Item item){
        if(item.getBidder()!= null){

        }
    }

    private void sendToBank(BankMessage bankMessage){
        try{
            out.writeObject(bankMessage);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        int clientPort = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);
        AuctionHouse server = new AuctionHouse(args[0],clientPort,serverPort);
    }
}
