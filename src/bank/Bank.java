package bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bank implements Runnable {
    private List<Account> accounts;
    private List<Integer> existingAccounts;
    public Bank() {
        accounts = new ArrayList<>();
        existingAccounts = new ArrayList<>();
    }

    /**
     * Create a new account w/ initial funds.
     * @param initialFunds int
     */
    public void createAccount(int initialFunds) {
        int number;
        do {
            Random rand = new Random();
            number = rand.nextInt(8999)+1000;
        }while(!existingAccounts.contains(number));
        existingAccounts.add(number);
        accounts.add(new Account(number, initialFunds));
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Random rand = new Random();
        for (int i = 0; i < 100000; i++) {
            System.out.println(rand.nextInt(8999) + 1000);
        }
    }
}

