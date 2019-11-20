package AuctionHouse;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import bankservice.*;
import bankservice.message.Message;
import com.sun.security.ntlm.Client;

public class AuctionHouse{
    private ServerSocket server;
    private Socket client;
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private ArrayList<Item> catalogue = new ArrayList<Item>();
    private int balance = 0;
    private AuctionHouse(String address,int clientPort, int serverPort){
        setupItemList();
            try{
                client = new Socket(address, clientPort);
                server = new ServerSocket(serverPort);
                Thread serverThread = new Thread(new AuctionServer());
                //serverThread.start();
                out    = new ObjectOutputStream(client.getOutputStream());
                Thread inThread = new Thread(new ClientIn());
                inThread.start();
            } catch(IOException u){
                System.out.println(u);
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

            /*try {
                assert out != null;
                out.flush();
                //input.close();
                out.close();
                client.close();
            }
            catch(IOException i) {
                System.out.println(i);
            }*/
    }

    private void setupItemList(){

    }

    private class AuctionServer implements Runnable{
        @Override
        public void run() {
            System.out.println("serverThread started");
            try{
                while(true){
                    Socket clientSocket = server.accept();
                    Thread.sleep(50);
                }
            }catch(IOException| InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private class ClientIn implements  Runnable{
        @Override
        public void run() {
            System.out.println("clientIn thread started");
            try{
                input  = new ObjectInputStream(client.getInputStream());
                Message message = (Message)input.readObject();
            }catch(IOException |ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        int clientPort = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);
        AuctionHouse server = new AuctionHouse(args[0],clientPort,serverPort);
    }
}
