package bank.test;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import shared.Message;
import shared.Message.Command;
import shared.NetInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class Client extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Client");
        GridPane grid = new GridPane();
        Text address = new Text("Server address: ");
        Text port = new Text("Port number: ");
        Text nameField = new Text("Name : ");

        TextField portNum = new TextField("4444");
        TextField serverNum = new TextField("192.168.0.110");
        TextField name = new TextField("ARGUS");

        grid.add(address, 1, 1);
        grid.add(serverNum, 2, 1);
        grid.add(port, 1, 2);
        grid.add(portNum, 2, 2);
        grid.add(nameField, 1, 3);
        grid.add(name, 2, 3);

        alert.getDialogPane().setContent(grid);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Integer nport = Integer.parseInt(portNum.getCharacters().toString());
                String naddress = serverNum.getCharacters().toString();
                String nname = name.getCharacters().toString();
                new Thread(new RunClient(nport, naddress, nname)).start();
            }
            if (response == ButtonType.CANCEL) {
                System.exit(5);
            }
            if (response == ButtonType.CLOSE) {
                System.exit(5);
            }
        });
    }

}


class RunClient implements Runnable {


    private final String address;
    private final Integer port;
    private final String name;

    public RunClient(Integer port, String address, String name) {
        this.port = port;
        this.address = address;
        this.name = name;
    }

    /**
     * not for bank, just used to test functions for bank
     */
    @Override
    public void run() {
        try {

            Socket client = new Socket(address, port);
            System.out.println("Connecting to: " + address + " ...");
            Scanner scanner = new Scanner(System.in);
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            MessageDisplay messageDisplay = new MessageDisplay(in);
            try {
                out.writeObject(new Message.Builder().command(Command.REGISTER_AH)
                        .netInfo(Collections.singletonList(new NetInfo(address, 4444)))
                        .send(null));
                Message initMessage = (Message) in.readObject();
                UUID accountId = initMessage.getAccountId();
                System.out.println(accountId);

                new Thread(messageDisplay).start();
                while (scanner.hasNext()) {

                    try {
                        List<String> text = Arrays.asList(scanner.nextLine().split(" "));

                        Double value = 0.;

                        if (text.size() > 1) {
                            try {
                                value = Double.parseDouble(text.get(1));

                            } catch (NumberFormatException e) {
                            }
                        }

                        switch (text.get(0)) {
                            case "block":
                                value = Double.parseDouble(text.get(1));
                                out.writeObject(new Message.Builder().command(Command.HOLD)
                                        .accountId(accountId)
                                        .amount(value)
                                        .send(accountId));
                                break;
                            case "unblock":
                                value = Double.parseDouble(text.get(1));
                                out.writeObject(new Message.Builder().command(Command.RELEASE_HOLD)
                                        .amount(value)
                                        .accountId(accountId)
                                        .send(accountId));
                                break;
                            case "deposit":
                                value = Double.parseDouble(text.get(1));
                                out.writeObject(new Message.Builder().command(Command.DEPOSIT)
                                        .amount(value).accountId(accountId).send(accountId));
                                break;
                            case "free":
                                out.writeObject(new Message.Builder().command(Command.GET_AVAILABLE)
                                        .send(accountId));
                                break;
                            case "locked":
                                out.writeObject(new Message.Builder().command(Command.GET_RESERVED)
                                        .send(accountId));
                                break;
                            case "balance":
                                out.writeObject(new Message.Builder().command(Command.GET_AVAILABLE)
                                        .send(accountId));
                                break;
                            case "deregister":
                                out.writeObject(new Message.Builder().command(Command.DEREGISTER_CLIENT).send(accountId));
                                break;
                            case "getinfo":
                                out.writeObject(new Message.Builder().command(Command.GET_NET_INFO).send(accountId));
                                break;
                        }
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                }
            } catch (ClassNotFoundException e) {
                // STUB
            }


        } catch (IOException e) {
            System.out.println("No server found");
        }

    }

    static class MessageDisplay implements Runnable {

        private final ObjectInputStream inputStream;

        MessageDisplay(ObjectInputStream inputStream) {
            this.inputStream = inputStream;
        }


        @Override
        public void run() {
            while (true) {
                Message received = null;
                try {
                    received = (Message) inputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                assert received != null;
                System.out.println(received.toString());
            }
        }
    }

}

