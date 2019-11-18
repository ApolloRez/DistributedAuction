package AuctionHouse;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionHouse{
    private ServerSocket server = null;
    private Socket client = null;
    private ObjectInputStream input   = null;
    private ObjectOutputStream out       =  null;
    private int balance = 0;

    private AuctionHouse(String address,int port){
            try{
                client = new Socket(address, port);
                System.out.println("Connected");
                server = new ServerSocket(3000);
                InetAddress ip = InetAddress.getLocalHost();
                NetInfo info = new NetInfo(ip.getHostAddress(),ip.getHostName()
                        ,server.getLocalPort());
                System.out.println("sup");
                out    = new ObjectOutputStream(client.getOutputStream());
                System.out.println("well hello there");
                //input  = new ObjectInputStream(client.getInputStream());
                System.out.println("test");
                out.writeObject(info);
                System.out.println("Sending "+info);
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

            try {
                assert out != null;
                out.flush();
                //input.close();
                out.close();
                client.close();
            }
            catch(IOException i) {
                System.out.println(i);
            }
    }

    public static void main(String args[]) {
        AuctionHouse server = new AuctionHouse("127.0.0.1",5000);
    }
}
