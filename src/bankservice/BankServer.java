package bankservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class BankServer {
    private Integer portNumber;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private List<Connection> connections;
    private Bank bank;

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
        connections = new ArrayList<>();
        isRunning = true;
        while (isRunning) {
            Connection connection = new Connection(serverSocket.accept(), this.bank);
            connections.add(connection);
            System.out.println(connection.getSocket().getInetAddress());
        }
    }

    /**
     * Change the boolean variable to false, canceling the bank server loop.
     */
    public void isNotRunning() {
        isRunning = false;
    }
}
