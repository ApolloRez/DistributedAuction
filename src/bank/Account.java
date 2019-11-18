package bank;

public class Account {
    private int accountNumber;
    private int availableBalance;

    public Account(int accountNumber, int availableBalance) {
        this.accountNumber = accountNumber;
        this.availableBalance = availableBalance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public int getAvailableBalance() {
        return availableBalance;
    }
}
