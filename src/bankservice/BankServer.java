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
     * @param portNumber
     * @param bank
     * @throws IOException
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
        }
    }

    public void isNotRunning() {
        isRunning = false;
    }
}
