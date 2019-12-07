package bank;

import bank.service.ConnectionLoggerService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;


public class BankServer implements Runnable {
    private final ServerSocket serverSocket;
    private boolean isRunning;
    private final Bank bank;

    /**
     * Create a Bank Server that accepts incoming client connections.
     *
     * @param portNumber Integer
     * @param bank       Bank
     * @throws IOException ServerSocket
     */
    public BankServer(Integer portNumber, Bank bank) throws IOException {
        this.bank = bank;
        serverSocket = new ServerSocket(portNumber);
        isRunning = true;
        ConnectionLoggerService connectionLoggerService =
                ConnectionLoggerService.getInstance();
        connectionLoggerService.add("Bank Server Started on "
                + InetAddress.getLocalHost().getHostName());
    }

    /**
     * Start this thread.
     */
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Connection connection = new Connection(serverSocket.accept(), this.bank);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
