package bank;
import java.util.UUID;

/**
 * Class used to represent a bank account of a client.
 */
public class Account {
    private UUID accountID;
    private double availableBalance;
    private double heldFunds;

    /**
     * Constructor
     * Taking a account id, initialize the account with default values.
     */
    public Account() {
        this.accountID = UUID.randomUUID();
        availableBalance = 0;
        heldFunds = 0;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public UUID getAccountID() {
        return accountID;
    }

    public double getHeldFunds() {
        return heldFunds;
    }

    /**
     * Deposit an amount of currency into this account.
     *
     * @param amount double
     */
    public void deposit(double amount) {
        System.out.println("depositing : " + amount);
        availableBalance += amount;
        System.out.println("Total Balance: " + availableBalance);
    }

    /**
     * Withdraw an amount of currency from this account. If there isn't enough
     * currency in the account, return false.
     * @param amount double
     * @return boolean
     */
    public boolean withdraw(double amount) {
        if (availableBalance - amount < 0) {
            return false;
        } else {
            availableBalance -= amount;
            return true;
        }
    }

    /**
     * Hold a given amount of currency, adding it to the total amount of funds
     * that is currently on hold. If there isn't enough currency to hold
     * then return false.
     * @param amount double
     * @return boolean
     */
    public boolean holdFunds(double amount) {
        if (availableBalance - amount < 0) {
            return false;
        } else {
            availableBalance -= amount;
            heldFunds += amount;
            return true;
        }
    }

    /**
     * Release the amount of currency from the hold total. If the amount to be
     * released is greater than the total held funds, return false.
     * @param amount double
     * @return boolean
     */
    public boolean releaseFunds(double amount) {
        if (heldFunds - amount < 0) {
            return false;
        } else {
            availableBalance += amount;
            heldFunds -= amount;
            return true;
        }
    }
}