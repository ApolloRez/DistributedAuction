package Agent;

import java.util.Scanner;

public class AgentDisplay {
    public Agent agent;
    public Scanner scanner;
    public AgentDisplay(Agent agent) {
        this.agent = agent;
        scanner = new Scanner(System.in);
    }

    public void startUp () {
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

        } else if (nextLine == "Balance") {

        } else if (nextLine == "Agent") {

        } else {
            System.out.println("Invalid Input, please try again, you can do it");
            bankMenu();
        }


    }

    private void depositMenu() {
        System.out.println("How much would you like to deposit? [1-1000]/[Back]");
        String deposit = scanner.nextLine();
        Integer depositAmount = Integer.parseInt(deposit);
        if (depositAmount <= 1000 && depositAmount > 0) {
            int cast = depositAmount;
            agent.bankDeposit(cast);
        }
    }
}

