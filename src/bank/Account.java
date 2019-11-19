package bank;

import java.util.UUID;

public class Account {
    private UUID accountID;
    private double availableBalance;

    public Account(UUID accountID) {
        this.accountID = accountID;
        availableBalance = 0;
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
}
