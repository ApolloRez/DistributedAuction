package bankservice;

import shared.NetInfo;
import java.util.*;

/**
 * Bank class that will play the roll as central bank for the Distributed
 * Auctions program. It consists of bookkeeping and a middle man for transactions
 * between its clients.
 */
public class Bank {
    private Map<UUID, Account> accounts;
    private List<NetInfo> auctionHouseNetInfo;

    /**
     * Constructor that initializes the bank, creating a data structure to
     * organize the banks clients.
     */
    public Bank() {
        accounts = new HashMap<>();
    }

    /**
     * Create a new account for a client and return the UUID.
     * @return UUID
     */
    public UUID createAccount() {
        return UUID.randomUUID();
    }

    /**
     * Deposit an amount of funds into the said account, if the account does
     * not exist in the data structure, return false.
     * @param accountID UUID
     * @param amount double
     * @return boolean deposit was successful
     */
    public boolean depositFunds(UUID accountID, double amount) {
        if (accounts.containsKey(accountID)) {
            accounts.get(accountID).deposit(amount);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Given two accounts, check if they exist, and if they do, attempt to
     * transfer funds from the client to the auctions house.
     * @param client UUID
     * @param auctionHouse UUID
     * @param amount double
     * @return boolean
     */
    public boolean transferFunds(UUID client, UUID auctionHouse, double amount) {
        if (accounts.containsKey(client) && accounts.containsKey(auctionHouse)) {
            if(accounts.get(client).withdraw(amount)) {
                accounts.get(auctionHouse).deposit(amount);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}