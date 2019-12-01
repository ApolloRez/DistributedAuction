package bank;

import java.io.IOException;

public class BankMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        BankServer bankServer = new BankServer(4444, new Bank());
        Thread thread = new Thread(bankServer);
        thread.start();
        thread.join();
    }
}