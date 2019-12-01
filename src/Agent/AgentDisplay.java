package Agent;

import java.util.LinkedList;
import java.util.Scanner;

public class AgentDisplay {
    public Agent agent;
    public Scanner scanner;

    public AgentDisplay(Agent agent) {
        this.agent = agent;
        scanner = new Scanner(System.in);
    }

    public void startUp() {
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

    private void bankMenu() {
        System.out.println("[Deposit]/[AuctionList]/[Balance]/[Agent]");
        String nextLine = scanner.nextLine();
        if (nextLine == "Deposit") {
            depositMenu();
        } else if (nextLine == "AuctionList") {
            auctionHouseList();
        } else if (nextLine == "Balance") {
            System.out.println("Current Balance: "+agent.getBalance());
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



    private void auctionHouseList() {
        LinkedList<String> auctionHouses = agent.getAuctionList();
        System.out.println("Auction Houses:");
        for (int i=1; i <= auctionHouses.size(); i++) {
            System.out.println("["+i+"] " + auctionHouses.get(i));
        }
        int choice = scanner.nextInt();
        if (agent.connectToAuctionHouse(choice)) {
            auctionHouseMenu();
        } else {
            System.out.println("error in connecting to auction house");
            bankMenu();
        }
    }

    private void auctionHouseMenu() {
        System.out.println("Welcome to auction house");
        System.out.println("Item currently up for bit:");
        printCurrentItem();


    }

    private void printCurrentItem() {
        /**
         * the agent gets a list of items to bid on from the auction house
         */
    }

    private void depositMenu() {
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

