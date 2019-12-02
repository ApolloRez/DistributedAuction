package bank.gui;

import bank.Bank;
import bank.BankServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BankGUI extends Application {
    private final SimpleDoubleProperty propertyWidth =
            new SimpleDoubleProperty();
    private final SimpleDoubleProperty propertyHeight =
            new SimpleDoubleProperty();

    @Override
    public void start(Stage primaryStage) throws Exception {
        new BankServer(4444, new Bank()).start();
        Scene scene = new Scene(new View().getRoot());
        primaryStage.setTitle("Bank");
        primaryStage.setHeight(400);
        primaryStage.setWidth(800);
        primaryStage.setScene(scene);
        propertyWidth.bind(scene.widthProperty());
        propertyHeight.bind(scene.heightProperty());
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
