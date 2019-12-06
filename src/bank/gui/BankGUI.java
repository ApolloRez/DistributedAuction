package bank.gui;

import bank.Bank;
import bank.BankServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BankGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Parameters parameters = this.getParameters();
        int portNumber;
        try {
            portNumber = Integer.parseInt(parameters.getRaw().get(0));
        } catch (NumberFormatException ignored) {
            portNumber = 4444;
        }
        try {
            new BankServer(portNumber, new Bank()).start();
        } catch (IOException ignored) {
            System.out.println("Invalid Port number");
        }
        Scene scene = new Scene(new View().getRoot());
        primaryStage.setTitle("Bank");
        primaryStage.setHeight(800);
        primaryStage.setWidth(1200);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
