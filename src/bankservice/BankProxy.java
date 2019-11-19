package bankservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BankProxy extends Bank{
    int portNumber = 4444;
    ServerSocket serverSocket = new ServerSocket(portNumber);
    public BankProxy() throws IOException {
        while(true) {
            Socket clientSocket = serverSocket.accept();

        }
    }
}
