package bank;

import bank.service.ConnectionLoggerService;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;


public class BankServer implements Runnable {
    private Integer portNumber;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private Bank bank;
    private final ConnectionLoggerService connectionLoggerService;

    /**
     * Create a Bank Server that accepts incoming client connections.
     *
     * @param portNumber Integer
     * @param bank       Bank
     * @throws IOException ServerSocket
     */
    public BankServer(Integer portNumber, Bank bank) throws IOException {
        this.portNumber = portNumber;
        this.bank = bank;
        serverSocket = new ServerSocket(this.portNumber);

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
        }
        isRunning = true;
        connectionLoggerService = ConnectionLoggerService.getInstance();

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
                connectionLoggerService.add(
                        connection.getSocket().getInetAddress().toString());
                System.out.println(connection.getSocket().getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
