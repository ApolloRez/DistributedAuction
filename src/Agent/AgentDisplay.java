package Agent;

import AuctionHouse.Item;
import shared.NetInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class AgentDisplay {
    public Agent agent;
    public Scanner scanner;

    public AgentDisplay(Agent agent) {
        this.agent = agent;
        scanner = new Scanner(System.in);
    }

    public static void printSuccess() {
        System.out.println("Successful");
    }

    public void printBalance() {
        System.out.println("Current Balance: " + agent.getBalance());
    }

    public void startUp() throws IOException {
        System.out.println("[Connect]/[Quit]");
        String nextLine = scanner.nextLine();
        if (nextLine == "Connect") {
            System.out.println("Attempting to Connect?");
            agent.registerBank();
            if (agent.connectedToBank) {
                bankMenu();
            } else {
                System.out.println("failed to connect please try again");
                startUp();
            }
        } else if (nextLine == "Quit") {
            agent.closeAgent();
        } else {
            startUp();
        }
    }

    private void bankMenu() throws IOException {
        System.out.println("[Deposit]/[AuctionList]/[Balance]/[Agent]");
        String nextLine = scanner.nextLine();
        agent.updateBalance();
        if (nextLine == "Deposit") {
            depositMenu();
        } else if (nextLine == "AuctionList") {
            auctionHouseList();
        } else if (nextLine == "Balance") {
            printBalance();
            bankMenu();
        } else if (nextLine == "Agent") {
            System.out.println(agent.idToString());
            bankMenu();
            /**
             * for testing / informative purposes
             * a way to print out information about the agent.
             */
        } else {
            System.out.println("Invalid Input, please try again, you can do it");
            bankMenu();
        }


    }



    private void auctionHouseList() throws IOException {
        List<NetInfo> auctionHouses = agent.getAuctionHouses();
        System.out.println("Auction Houses:");
        int goBack = auctionHouses.size()+1;
        for (int i=1; i <= auctionHouses.size(); i++) {
            System.out.println("["+i+"] " + auctionHouses.get(i).toString());
        }
        System.out.println("["+goBack+"] go back");
        int choice = scanner.nextInt();
        if (choice == goBack) {
            bankMenu();
        } else if (agent.connectToAuctionHouse(choice)) {
            auctionHouseMenu();
        } else {
            System.out.println("error in connecting to auction house");
            bankMenu();
        }
    }


    private void auctionHouseMenu() throws IOException {
        System.out.println("Items currently up for bid:");
        printCurrentItems();
        int goBack = 1 + agent.getCatalogue().size();
        System.out.println("["+goBack+"] Go back]");
        int choice = scanner.nextInt();
        if (choice == agent.getCatalogue().size()+2) {
            agent.deRegisterAuctionHouse();
            bankMenu();
        } else {
            System.out.println("Bid:");
            double bid = scanner.nextDouble();
            agent.sendBidToAH(int choice, double bid);


        }
    }




    private void printCurrentItems() {
        ArrayList<Item> catalogue = agent.getCatalogue();
        for (int i=0; i < catalogue.size(); i++) {
            System.out.println("["+i+"]"+catalogue.get(i-1).name());
            System.out.println("Current Bid: "+catalogue.get(i-1).value());
            if (catalogue.get(i).getBidder()==agent.getAccountNumber()) {
                System.out.println("Currently Winning\n");
            }
        }


    }

    private void depositMenu() throws IOException {
        System.out.println("How much would you like to deposit? [1-1000]/[Back]");
        String deposit = scanner.nextLine();
        Integer depositAmount = Integer.parseInt(deposit);
        if (depositAmount <= 1000 && depositAmount > 0) {
            int cast = depositAmount;
            agent.bankDeposit(cast);
            System.out.println("Successful, new balance: "+agent.getBalance());
            bankMenu();
        } else if (deposit == "Back") {
            bankMenu();
        } else {
            System.out.println("Sorry, try again");
            depositMenu();
        }
    }
}