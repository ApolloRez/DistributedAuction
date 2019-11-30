package bankservice;

import shared.BankMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Runnable {

    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private final Bank bank;
    private boolean running = true;

    public Connection(Socket socket, Bank bank) throws IOException {
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.bank = bank;
        new Thread(this).start();
    }

    /**
     * Stop the running thread.
     */
    public void closeThread() {
        running = false;
    }

    @Override
    public void run() {
        try {
            BankMessage bankMessage;
            bankMessage = (BankMessage) objectInputStream.readObject();
            if (bankMessage.getCommand() == BankMessage.Command.REGISTER_CLIENT) {
                objectOutputStream.writeObject(
                        new BankMessage.Builder()
                                .accountId(bank.registerClient())
                                .send(bank.getId()));
            }
            if (bankMessage.getCommand() == BankMessage.Command.REGISTER_AH) {
                objectOutputStream.writeObject(
                        new BankMessage.Builder()
                                .accountId(bank.registerAuctionHouse(
                                        bankMessage.getNetInfo().get(0)))
                                .send(bank.getId()));
            }
            while (running) {
                bankMessage = (BankMessage) objectInputStream.readObject();
                switch (bankMessage.getCommand()) {
                    case DEPOSIT: {
                        bank.depositFunds(bankMessage.getSender(),
                                bankMessage.getAmount());
                        break;
                    }
                    case HOLD: {
                        if (bank.holdFunds(bankMessage.getAccountId(),
                                bankMessage.getAmount())) {
                            writeMessage(new BankMessage.Builder()
                                    .response(BankMessage.Response.SUCCESS)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new BankMessage.Builder()
                                    .response(BankMessage.Response.INSUFFICIENT_FUNDS)
                                    .send(bank.getId()));
                        }
                    }
                    case GET_AVAILABLE: {
                        writeMessage(new BankMessage.Builder().amount(
                                bank.getAccountFunds(bankMessage.getAccountId()))
                                .send(bank.getId()));
                    }
                }
            }
        } catch (IOException |
                ClassNotFoundException |
                NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void writeMessage(BankMessage bankMessage) throws IOException {
        objectOutputStream.writeObject(bankMessage);
    }

    private BankMessage readMessage() throws IOException, ClassNotFoundException {
        return (BankMessage) objectInputStream.readObject();
    }
}
