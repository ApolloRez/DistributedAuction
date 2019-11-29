package bankservice;

import bankservice.message.Message;

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
            Message message;
            message = (Message) objectInputStream.readObject();
            if(message.getCommand() == Message.Command.REGISTER_CLIENT) {
                objectOutputStream.writeObject(
                        new Message.Builder()
                                .accountId(bank.registerClient())
                                .send(bank.getId()));
            }
            if (message.getCommand() == Message.Command.REGISTER_AH) {
                objectOutputStream.writeObject(
                        new Message.Builder()
                                .accountId(bank.registerAuctionHouse(
                                        message.getNetInfo().get(0)))
                                .send(bank.getId()));
            }
            while (running) {
                message = (Message) objectInputStream.readObject();
                switch (message.getCommand()) {
                    case DEPOSIT: {
                        bank.depositFunds(message.getSender(),
                                message.getAmount());
                        break;
                    }
                    case HOLD: {
                        if(bank.holdFunds(message.getAccountId(),
                                message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.SUCCESS)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.INSUFFICIENT_FUNDS)
                                    .send(bank.getId()));
                        }
                    }
                    case GET_AVAILABLE: {
                        writeMessage(new Message.Builder().amount(
                                bank.
                        ));
                    }
                }
            }
        } catch (IOException |
                ClassNotFoundException |
                NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void writeMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }
    private Message readMessage() throws IOException, ClassNotFoundException {
        return (Message)objectInputStream.readObject();
    }
}
