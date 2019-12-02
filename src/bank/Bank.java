package bank;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ObservableList<NetInfo> auctionHouseNetInfo;

    /**
     * Constructor that initializes the bank, creating a data structure to
     * organize the banks clients.
     */
    public Bank() {
        auctionHouseNetInfo = FXCollections.observableArrayList();
        accounts = new HashMap<>();
        id = UUID.randomUUID();
    }

    /**
     * Get the id of this bank.
     *
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
        Account account = new Account();
        accounts.put(account.getAccountID(), account);
        return account.getAccountID();
    }

    /**
     * Deposit an amount of funds into the said account, if the account does
     * not exist in the data structure, return false.
     *
     * @param targetId UUID
     * @param amount   double
     */
    public synchronized void depositFunds(UUID targetId, double amount) {
        accounts.get(targetId).deposit(amount);
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
    public synchronized boolean transferFunds(UUID client,
                                              UUID auctionHouse,
                                              double amount) {
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
     * @param amount    double
     * @return boolean
     */
    public synchronized boolean holdFunds(UUID accountId, double amount) {
        if (accounts.containsKey(accountId)) {
            return accounts.get(accountId).holdFunds(amount);
        }
        return false;
    }

    /**
     * Release the specified amount of funds from the held funds.
     *
     * @param accountId UUID
     * @param amount    double
     * @return boolean
     */
    public synchronized boolean releaseFunds(UUID accountId, double amount) {
        if (accounts.containsKey(accountId)) {
            return accounts.get(accountId).releaseFunds(amount);
        }
        return false;
    }

    /**
     * Return the available balance of said account.
     *
     * @param targetId UUID
     * @return double
     */
    public synchronized double getAccountFunds(UUID targetId) {
        return accounts.get(targetId).getAvailableBalance();
    }

    public synchronized double getHeldFunds(UUID targetId) {
        return accounts.get(targetId).getHeldFunds();
    }
}