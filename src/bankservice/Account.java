package bankservice;

import java.util.UUID;

public class Account {
    private UUID accountID;
    private double availableBalance;
    private double heldFunds;

    public Account(UUID accountID) {
        this.accountID = accountID;
        availableBalance = 0;
        heldFunds = 0;
    }
    public void deposit(double amount) {
        availableBalance += amount;
    }
    public boolean withdraw(double amount) {
        if (availableBalance - amount < 0) {
            return false;
        } else {
            availableBalance -= amount;
            return true;
        }
    }
    public boolean holdFunds(double amount) {
        if (availableBalance - amount < 0) {
            return false;
        } else {
            availableBalance -= amount;
            heldFunds += amount;
            return true;
        }
    }
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
