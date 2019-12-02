package AuctionHouse;

import AuctionHouse.AuctionHouse;
import bank.gui.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import shared.AuctionMessage;
import java.util.ArrayList;

public class AuctionGui extends Application {
    private BorderPane bPane = new BorderPane();
    private TextField ipInputField = new TextField("10.1.10.57");
    private TextField portInput = new TextField("5000");
    private TextField serverInput = new TextField("4444");
    private ArrayList<Item> catalogue = new ArrayList<>();
    private VBox listDisplay = new VBox();
    private AuctionHouse auction;
    private boolean done = false;
    @Override
    public void start(Stage stage) {

        topWindowSetup();
        //View logger = new View();
        //bPane.setBottom(logger.getRoot());
        Scene scene = new Scene(bPane,400,400);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setTitle("Auction House");
        stage.setScene(scene);
        stage.show();
    }

    public void topWindowSetup(){
        HBox topWindow = new HBox();
        topWindow.setAlignment(Pos.CENTER);
        VBox bankIP = new VBox();
        Text ipText = new Text("Bank IP");
        bankIP.getChildren().addAll(ipText,ipInputField);
        topWindow.getChildren().add(bankIP);

        VBox bankPort = new VBox();
        Text portText = new Text("Bank Port");
        portInput.setPrefWidth(60);
        bankPort.getChildren().addAll(portText,portInput);
        topWindow.getChildren().add(bankPort);

        VBox serverPort = new VBox();
        Text serverText = new Text("Auction port");
        serverPort.setPrefWidth(60);
        serverPort.getChildren().addAll(serverText,serverInput);
        topWindow.getChildren().add(serverPort);

        Button connect = new Button("connect");
        EventHandler<ActionEvent> event = e -> createAuctionHouse();
        connect.setOnAction(event);
        topWindow.getChildren().add(connect);
        bPane.setTop(topWindow);
    }

    public void createAuctionHouse(){
        String bankIp = ipInputField.getText();
        int bankPort = Integer.parseInt(portInput.getText());
        int serverPort = Integer.parseInt(serverInput.getText());
        auction = new AuctionHouse(bankIp,bankPort,serverPort);
        catalogue = auction.getCatalogue();
        setupCatalogue();
        Thread thread = new Thread(uiUpdater);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupCatalogue(){
        ScrollPane display = new ScrollPane();
        for (Item item : catalogue) {
            VBox guiItem = new VBox();
            Text name = new Text(item.name());
            guiItem.getChildren().add(name);
            listDisplay.getChildren().add(guiItem);
        }
        display.setContent(listDisplay);
        bPane.setCenter(display);
    }

    private Runnable uiUpdater = () ->{
        Runnable updater = this::update;
        while(!done){
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    private void update(){
        listDisplay.getChildren().clear();
        for (Item item : catalogue) {
            VBox guiItem = new VBox();
            Text name = new Text(item.name());
            guiItem.getChildren().add(name);
            listDisplay.getChildren().add(guiItem);
        }
    }

    public static void main(String args[]){
        launch(args);
    }
}
