package bank;

import bank.service.ConnectionLoggerService;
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
    private final ConnectionLoggerService connectionLoggerService;
    private boolean running = true;
    private Message.Command connectionType = null;
    /**
     * The connection object handles the communication between the bank and
     * the clients.
     *
     * @param socket Socket
     * @param bank   Bank
     * @throws IOException Socket disconnect
     */
    public Connection(Socket socket, Bank bank) throws IOException {
        connectionLoggerService = ConnectionLoggerService.getInstance();
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.bank = bank;
        new Thread(this).start();
    }

    /**
     * Stop this running thread.
     */
    private void closeThread() {
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
            connectionLoggerService.add("Connection Join: "
                    + socket.getInetAddress().getHostName());
            Message message = readMessage();
            if (message.getCommand() == Message.Command.REGISTER_CLIENT) {
                connectionType = Message.Command.REGISTER_CLIENT;
                writeMessage(new Message.Builder()
                        .accountId(bank.registerClient())
                        .response(Message.Response.SUCCESS)
                        .command(Message.Command.REGISTER_CLIENT)
                        .send(bank.getId()));
            }
            if (message.getCommand() == Message.Command.REGISTER_AH) {
                connectionType = Message.Command.REGISTER_AH;
                writeMessage(new Message.Builder()
                        .accountId(bank.registerAuctionHouse(
                                message.getNetInfo().get(0)))
                        .response(Message.Response.SUCCESS)
                        .command(Message.Command.REGISTER_AH)
                        .send(bank.getId()));
            }
            while (running) {
                message = readMessage();
                switch (message.getCommand()) {
                    // UUID - senderID
                    // double - amount
                    case DEPOSIT: {
                        bank.depositFunds(message.getSender(),
                                message.getAmount());
                        writeMessage(new Message.Builder()
                                .response(Message.Response.SUCCESS)
                                .command(Message.Command.DEPOSIT)
                                .amount(message.getAmount())
                                .send(bank.getId()));
                        break;
                    }
                    // UUID - accountId
                    // double - amount
                    case HOLD: {
                        if (bank.holdFunds(message.getAccountId(),
                                message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.SUCCESS)
                                    .command(Message.Command.HOLD)
                                    .amount(message.getAmount())
                                    .accountId(message.getAccountId())
                                    .send(bank.getId()));
                            System.out.println(message.getAmount());
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(
                                            Message.Response.INSUFFICIENT_FUNDS)
                                    .command(Message.Command.HOLD)
                                    .accountId(message.getAccountId())
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    // UUID - accountId
                    // double - amount
                    case RELEASE_HOLD: {
                        if (bank.releaseFunds(message.getAccountId(),
                                message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .amount(message.getAmount())
                                    .response(Message.Response.SUCCESS)
                                    .command(Message.Command.RELEASE_HOLD)
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(
                                            Message.Response.INSUFFICIENT_FUNDS)
                                    .command(Message.Command.RELEASE_HOLD)
                                    .amount(message.getAmount())
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    // UUID - senderId
                    // UUID - accountId
                    // double - amount
                    case TRANSFER: {
                        if (bank.transferFunds(message.getSender(),
                                message.getAccountId(), message.getAmount())) {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.SUCCESS)
                                    .command(Message.Command.TRANSFER)
                                    .amount(message.getAmount())
                                    .accountId(message.getAccountId())
                                    .send(bank.getId()));
                        } else {
                            writeMessage(new Message.Builder()
                                    .response(Message.Response.ERROR)
                                    .command(Message.Command.TRANSFER)
                                    .amount(message.getAmount())
                                    .accountId(message.getAccountId())
                                    .send(bank.getId()));
                        }
                        break;
                    }
                    // UUID - senderId
                    case GET_AVAILABLE: {
                        writeMessage(new Message.Builder()
                                .amount(bank.getAccountFunds(message.getSender()))
                                .command(Message.Command.GET_AVAILABLE)
                                .send(bank.getId()));
                        break;
                    }
                    case GET_NET_INFO: {
                        writeMessage(new Message.Builder()
                                .command(Message.Command.GET_NET_INFO)
                                .netInfo(bank.getAuctionHouseNetInfo())
                                .send(bank.getId()));
                        break;
                    }
                    // UUID - senderId
                    case GET_RESERVED: {
                        writeMessage(new Message.Builder()
                                .command(Message.Command.GET_RESERVED)
                                .amount(bank.getHeldFunds(message.getSender()))
                                .send(bank.getId()));
                        break;
                    }
                    // UUID - sender
                    // List<NetInfo> netInfo
                    case DEREGISTER_AH: {
                        bank.deRegisterAuctionHouse(message.getSender(),
                                message.getNetInfo().get(0).getIp());
                        connectionLoggerService.add("Connection dropped : "
                                + socket.getInetAddress().getHostName());
                        this.closeThread();
                        break;
                    }
                    // UUID - sender
                    case DEREGISTER_CLIENT: {
                        bank.deRegisterClient(message.getSender());
                        connectionLoggerService.add("Connection dropped : "
                                + socket.getInetAddress().getHostName());
                        this.closeThread();
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException |
                NullPointerException ignored) {
            try {
                writeMessage(new Message.Builder()
                        .response(Message.Response.INVALID_PARAMETERS)
                        .send(bank.getId()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            if (connectionType == Message.Command.REGISTER_AH) {
                bank.auctionHouseConnDrop(
                        socket.getInetAddress().getHostAddress());
            }
            connectionLoggerService.add("Connection dropped : "
                    + socket.getInetAddress().getHostName());
            this.closeThread();
        }
    }

    /**
     * Write a message to the ObjectOutputStream.
     *
     * @param message Message
     * @throws IOException Cannot write to ObjectOutputStream
     */
    private void writeMessage(Message message) throws IOException {
        Message.Command temp = message.getCommand();
        if (temp != Message.Command.GET_RESERVED && temp != Message.Command.GET_AVAILABLE) {
            connectionLoggerService.add("Bank : " + message.toString());
        }
        objectOutputStream.reset();
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
        Message message = (Message) objectInputStream.readObject();
        Message.Command temp = message.getCommand();
        if (temp != Message.Command.GET_RESERVED && temp !=
                Message.Command.GET_AVAILABLE) {
            if (connectionType == Message.Command.REGISTER_CLIENT) {
                connectionLoggerService.add("\t\tClient : " + message.toString());
            } else {
                connectionLoggerService.add("Auction House" + message.toString());
            }
        }
        return message;
    }
}