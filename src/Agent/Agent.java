package Agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Agent {

    private ServerSocket server = null;
    private Socket client = null;
    private double balance;
    private int accountNumber;

    public Agent(String hostName, int portNumber) {

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in =
                     new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )  {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer = in.readLine();
            while(fromServer != null) {
                System.out.println("Bank: ");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connectToAuctionHouse(/*IP or somethin?*/) {
        /*
        "The agent gets a list of active auction houses from the bank.
         In connects to an auction
        house using the host and port information sent from the bank."
         */
    }

    public AuctionENUMs makeBid(/*Item to bid on and price?*/) {
        /*
        make a bid on an item at the auction house and return
        one of the ENUM regarding the result.
         */
        return null;
    }

    public void contactBank(AuctionENUMs result) {
        /*
        if the agent win it must contact the bank to unblock and transfer the funds.

        this might not be the exact best method to handle all of the results.
         */
    }

    public static void main(String[] args) {


    }





}
