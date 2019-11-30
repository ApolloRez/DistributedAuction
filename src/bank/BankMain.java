package bank;

import java.io.IOException;

public class BankMain {

    public static void main(String[] args) throws IOException {
        BankServer bankServer = new BankServer(4444, new Bank());
    }
}