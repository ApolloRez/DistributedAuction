package bankservice;

import shared.NetInfo;

import java.util.*;

/**
 * Bank class that will play the roll as central bank for the Distributed
 * Auctions program. It consists of bookkeeping and a middle man for transactions
 * between its clients.
 */
public class Bank {
    private final UUID id;
    private Map<UUID, Account> accounts;
    private List<NetInfo> auctionHouseNetInfo;

    /**
     * Constructor that initializes the bank, creating a data structure to
     * organize the banks clients.
     */
    public Bank() {
        accounts = new HashMap<>();
        id = UUID.randomUUID();
    }

    /**
     * Get the id of this bank.
     * @return {@link UUID}
     */
    public UUID getId() {
        return id;
    }

    /**
     * Add the auction house NetInfo to the List and return a new
     * account id.
     *
     * @param netInfo NetInfo
     * @return UUID
     */
    public UUID registerAuctionHouse(NetInfo netInfo) {
        auctionHouseNetInfo.add(netInfo);
        return createAccount();
    }

    /**
     * Register a client account with this bank and return the id of the account.
     *
     * @return UUID
     */
    public UUID registerClient() {
        return createAccount();
    }

    /**
     * Get an unmodifiable list of the auction house net information.
     *
     * @return List<NetInfo>
     */
    public List<NetInfo> getAuctionHouseNetInfo() {
        return Collections.unmodifiableList(auctionHouseNetInfo);
    }

    /**
     * Create a new account for a client and return the UUID.
     *
     * @return UUID
     */
    private UUID createAccount() {
        return UUID.randomUUID();
    }

    /**
     * Deposit an amount of funds into the said account, if the account does
     * not exist in the data structure, return false.
     *
     * @param accountID UUID
     * @param amount    double
     * @return boolean deposit was successful
     */
    public synchronized boolean depositFunds(UUID accountID, double amount) {
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
     *
     * @param client       UUID
     * @param auctionHouse UUID
     * @param amount       double
     * @return boolean
     */
    public synchronized boolean transferFunds(UUID client, UUID auctionHouse, double amount) {
        if (accounts.containsKey(client) && accounts.containsKey(auctionHouse)) {
            if (accounts.get(client).withdraw(amount)) {
                accounts.get(auctionHouse).deposit(amount);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Given a accountId accountId, place a hold on the funds of the given amount.
     *
     * @param accountId UUID
     * @param amount double
     * @return boolean
     */
    public synchronized boolean holdFunds(UUID accountId, double amount) {
        if (accounts.containsKey(accountId)) {
            return accounts.get(accountId).holdFunds(amount);
        }
        return false;
    }

    /**
     * Return the available balance of said account.
     * @param accountId UUID
     * @return double
     */
    public double getAccountFunds(UUID accountId) {
        return accounts.get(accountId).getAvailableBalance();
    }
}