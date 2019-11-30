package bankservice;

import shared.Message;

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

    /**
     * The connection object handles the communication between the bank and
     * the clients.
     *
     * @param socket Socket
     * @param bank   Bank
     * @throws IOException
     */
    public Connection(Socket socket, Bank bank) throws IOException {
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.bank = bank;
        new Thread(this).start();
    }

    /**
     * Get the socket object.
     *
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Stop this running thread.
     */
    public void closeThread() {
        running = false;
    }

    /**
     * Run method that handles the logic of incoming messages.
     * Upon receiving a message object, parse it and return a
     * message to the sender respectively.
     */
    @Override
    public void run() {
        try {
            Message message = readMessage();
            if (message.getCommand() == Message.Command.REGISTER_CLIENT) {
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
                message = readMessage();
                System.out.println(message);
                switch (message.getCommand()) {
                    case DEPOSIT: {
                        bank.depositFunds(message.getSender(),
                                message.getAmount());
                        writeMessage(new Message.Builder()
                                .response(Message.Response.SUCCESS)
                                .send(bank.getId()));
                        break;
                    }
                    case HOLD: {
                        if (bank.holdFunds(message.getTargetId(),
                                message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.SUCCESS)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.INSUFFICIENT_FUNDS)
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    case RELEASE_HOLD: {
                        if (bank.releaseFunds(message.getTargetId(),
                                message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .amount(message.getAmount())
                                    .response(Message.Response.SUCCESS)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.INSUFFICIENT_FUNDS)
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    case TRANSFER: {
                        if (bank.transferFunds(message.getTargetId(),
                                message.getSender(), message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.SUCCESS)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.ERROR)
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    case GET_AVAILABLE: {
                        writeMessage(new Message.Builder().amount(
                                bank.getAccountFunds(message.getSender()))
                                .send(bank.getId()));
                        break;
                    }
                    case GET_NET_INFO: {
                        writeMessage(new Message.Builder()
                                .netInfo(bank.getAuctionHouseNetInfo())
                                .send(bank.getId()));
                        break;
                    }
                    case GET_RESERVED: {
                        writeMessage(new Message.Builder()
                                .amount(bank.getHeldFunds(message.getTargetId()))
                                .send(bank.getId()));
                        break;
                    }
                }
            }
        } catch (IOException |
                ClassNotFoundException |
                NullPointerException ignored) {
            try {
                writeMessage(new Message.Builder().response(Message.Response.INVALID_PARAMETERS).send(bank.getId()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Write a message to the ObjectOutputStream.
     *
     * @param message Message
     * @throws IOException
     */
    private void writeMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }

    /**
     * Read a message from the ObjectInputStream and return the object.
     *
     * @return Message
     * @throws IOException            Connection broken
     * @throws ClassNotFoundException Message class not found
     */
    private Message readMessage() throws IOException, ClassNotFoundException {
        return (Message) objectInputStream.readObject();
    }
}
