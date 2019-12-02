package bank;

import javafx.collections.FXCollections;
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
     * Remove the AuctionHouse from the netInfo list as well as the accounts.
     *
     * @param accountId UUID
     * @param netInfo   {@link} NetInfo
     */
    public void deRegisterAuctionHouse(UUID accountId, NetInfo netInfo) {
        auctionHouseNetInfo.remove(netInfo);
        accounts.remove(accountId);
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
     * Remove client from he accounts list.
     *
     * @param accountId {@link} UUID
     */
    public void deRegisterClient(UUID accountId) {
        accounts.remove(accountId);
    }

    public void auctionHouseConnDrop(String iNetAddress) {
        NetInfo delete = null;
        for (NetInfo netInfo : auctionHouseNetInfo) {
            if (iNetAddress.equals(netInfo.getIp())) {
                delete = netInfo;
            }
        }
        auctionHouseNetInfo.remove(delete);
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
     * transfer funds from the sender to the auctions house.
     *
     * @param sender    UUID
     * @param recipient UUID
     * @param amount    double
     * @return boolean
     */
    public synchronized boolean transferFunds(UUID sender,
                                              UUID recipient,
                                              double amount) {
        if (accounts.containsKey(sender) && accounts.containsKey(recipient)) {
            if (accounts.get(sender).withdraw(amount)) {
                accounts.get(recipient).deposit(amount);
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
     * @param accountId UUID
     * @return double
     */
    public synchronized double getAccountFunds(UUID accountId) {
        return accounts.get(accountId).getAvailableBalance();
    }

    /**
     * Get the amount of held funds for said account.
     *
     * @param accountId UUID
     * @return double
     */
    public synchronized double getHeldFunds(UUID accountId) {
        return accounts.get(accountId).getHeldFunds();
    }
}