package bank.gui;

import bank.Bank;
import bank.BankServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BankGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        new BankServer(4444, new Bank()).start();
        Scene scene = new Scene(new View().getRoot());
        primaryStage.setTitle("Bank");
        primaryStage.setHeight(400);
        primaryStage.setWidth(1200);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
