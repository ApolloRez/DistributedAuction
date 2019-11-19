package bank;

import java.util.*;

public class Bank {
    private Map<UUID, Account> accounts;
    public Bank() {
        accounts = new HashMap<>();
    }
    public UUID createAccount() {
        return UUID.randomUUID();
    }
    public boolean depositFunds(UUID accountID, double amount) {
        if (accounts.containsKey(accountID)) {
            accounts.get(accountID).deposit(amount);
            return true;
        } else {
            return false;
        }
    }
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

