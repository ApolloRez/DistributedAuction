package Agent;

import AuctionHouse.Item;
import shared.NetInfo;

import java.io.IOException;
import java.util.ArrayList;
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

    public void updateAuctionMenu() throws IOException {
        auctionHouseMenu();
    }

    public void printBalance() {
        System.out.println("Current Balance: " + agent.getBalance());
    }

    public void printDepositBalance() throws IOException {
        System.out.println("New Balance: " + agent.getBalance());

    }

    public void startUp() throws IOException, InterruptedException {
        System.out.println("[Connect]/[Quit]");
        String nextLine = scanner.nextLine();
        System.out.println(nextLine);
        if (nextLine.equals("Connect")) {
            System.out.println("Attempting to Connect?");
            agent.registerBank();
           // wait(); ?
            agent.connectedToBank = true;
            if (agent.connectedToBank) {
                bankMenu();
            } else {
                System.out.println("failed to connect please try again");
                startUp();
            }
        } else if (nextLine.equals("Quit")) {
            agent.closeAgent();
        } else {
            System.out.println("goofing");
            startUp();
        }
    }

    private void bankMenu() throws IOException {
        System.out.println("[Deposit]/[AuctionList]/[Balance]/[Agent]");
        String nextLine = scanner.nextLine();
        agent.updateBalance();
        if (nextLine.equals("Deposit")) {
            depositMenu();
        } else if (nextLine.equals("AuctionList")) {
            auctionHouseList();
        } else if (nextLine.equals("Balance")) {
            printBalance();
            bankMenu();
        } else if (nextLine.equals("Agent")) {
            System.out.println("ID: "+agent.idToString());
            System.out.println("Won Items:");
            for(Item item: agent.getWonItems()) {
                System.out.println("- "+item.name());
            }
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
        agent.requestAHList();

    }

    public void printAHList(List<NetInfo> auctionHouses) throws IOException {
        System.out.println("Auction Houses:");
        printBalance();
        int goBack = auctionHouses.size();
        for (int i=0; i < auctionHouses.size(); i++) {
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

    /**
     * this has potential to be bad.  It might dork up when we try
     *   to update the catalogue from the auction house.
     * @throws IOException
     */
    public void auctionHouseMenu() throws IOException {
        int choice = -1;
        double bid = -1;
        System.out.println("Items currently up for bid:");
        printBalance();
        printCurrentItems();
        int goBack = 1 + agent.getStoredCat().size();
        System.out.println("["+goBack+"] Go back]");
        choice = scanner.nextInt();
        if (choice == agent.getStoredCat().size()+1) {
            agent.deRegisterAuctionHouse();
            bankMenu();
        } else {
            System.out.println("Bid:");
            bid = scanner.nextDouble();
            agent.sendBidToAH(choice, bid);
        }
    }




    private void printCurrentItems() {
        ArrayList<Item> catalogue = agent.getStoredCat();
        for (int i=0; i < catalogue.size(); i++) {
            System.out.println("["+i+"]"+catalogue.get(i).name());

            System.out.println("Current Bid: "+catalogue.get(i).getCurrentBid());
            System.out.println("Starting Bid: "+catalogue.get(i).getMinimumBid());

            if (catalogue.get(i).getBidder()==agent.getAccountNumber()) {
                System.out.println("Currently Winning\n");
            }
        }


    }

    private void depositMenu() throws IOException {
        System.out.println("How much would you like to deposit? [1-1000]/[Back]");
        String deposit = scanner.nextLine();
        if (deposit.equals("Back")) {
            bankMenu();
        }
        double cast = 0;
        try {
            Double depositAmount = Double.parseDouble(deposit);
            cast = depositAmount;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (cast <= 1000 && cast > 0) {
            agent.bankDeposit(cast);
            //System.out.println("Successful, new balance: "+agent.getBalance());
            bankMenu();
        } else {
            System.out.println("Sorry, try again");
            depositMenu();
        }
    }

    public void wonAnItem() {

    }
}