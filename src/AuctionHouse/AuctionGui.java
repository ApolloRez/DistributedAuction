package AuctionHouse;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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

import java.util.ArrayList;

/**
 * @author Steven Chase
 * This class is dedicated to displaying the AuctionHouse object with items for
 * sale, log of activity, id of auction house, and bank balance
 * of auction house. It also provides some UI for interaction/input.
 */
public class AuctionGui extends Application {
    /**
     * bPane: BorderPane holding everything.
     * ipInputField: textfield to input ip of bank
     * portInput: TextField to input port number of bank
     * serverInput: TextField to input desired port number of auction server
     * catalogue: items currently for sale at the auction house
     * listDisplay: VBox for displaying the current items for sale with info
     * auction: the auction house
     * disconnect: button to shutdown the auction house
     * connect: button connect to the bank with given ip and port number
     * vLog: VBox for displaying auction house log of activity
     * id: Text for displaying auction house id
     * balance: Text for displaying balance of auction house
     * log: List of incoming/outgoing messages
     * done: boolean to end the program
     */
    private BorderPane bPane = new BorderPane();
    private TextField ipInputField = new TextField("");
    private TextField portInput = new TextField("");
    private TextField serverInput = new TextField("");
    private ArrayList<Item> catalogue = new ArrayList<>();
    private VBox listDisplay = new VBox();
    private AuctionHouse auction;
    private Button disconnect = new Button("Shutdown");
    private Button connect = new Button("connect");
    private VBox vLog = new VBox();
    private Text id = new Text("ID:");
    private Text balance = new Text("balance: ");
    private ArrayList<String> log = new ArrayList<>();
    private boolean done = false;

    /**
     * starts the Gui
     * @param stage the stage
     */
    @Override
    public void start(Stage stage) {
        topWindowSetup();
        setupLog();
        setupLeftWindow();
        setupCatalogue();
        Scene scene = new Scene(bPane,500,450);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        EventHandler<ActionEvent> event = e -> shutdown();
        disconnect.setOnAction(event);
        disconnect.setDisable(true);
        stage.setTitle("Auction House");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * setups the top window of bPane along with the UI
     */
    private void topWindowSetup(){
        HBox topWindow = new HBox();
        topWindow.setAlignment(Pos.CENTER);
        VBox bankIP = new VBox();
        Text ipText = new Text("Bank IP");
        bankIP.setPrefWidth(80);
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

        EventHandler<ActionEvent> event = e -> createAuctionHouse();
        connect.setOnAction(event);
        topWindow.getChildren().add(connect);
        topWindow.getChildren().add(disconnect);
        topWindow.setSpacing(10);
        bPane.setTop(topWindow);
    }

    /**
     * setups the Left port of bPane with id, balance
     */
    private void setupLeftWindow(){
        VBox left = new VBox();
        left.setSpacing(10);
        left.getChildren().add(id);
        left.getChildren().add(balance);
        bPane.setLeft(left);
    }

    /**
     * setups the log for display
     */
    private void setupLog(){
        ScrollPane logDisplay = new ScrollPane();
        logDisplay.setPrefViewportHeight(150);
        logDisplay.setFitToWidth(true);
        logDisplay.setContent(vLog);
        logDisplay.vvalueProperty().bind(vLog.heightProperty());
        bPane.setBottom(logDisplay);
    }

    /**
     * called when the "connect" button is pressed. This method
     * starts the ui with the given input from the TextFields and starts
     * the uiUpdater thread
     */
    private void createAuctionHouse(){
        done = false;
        String bankIp = ipInputField.getText();
        int bankPort = Integer.parseInt(portInput.getText());
        int serverPort = Integer.parseInt(serverInput.getText());
        auction = new AuctionHouse(bankIp,bankPort,serverPort);
        Thread thread = new Thread(uiUpdater);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Setups the VBox used to display the catalogue of AuctionHouse Items for
     * sale
     */
    private void setupCatalogue(){
        ScrollPane display = new ScrollPane();
        listDisplay.setSpacing(5);
        listDisplay.setAlignment(Pos.CENTER);
        display.setContent(listDisplay);
        Insets inset = new Insets(10);
        bPane.setCenter(display);
        BorderPane.setMargin(display,inset);
        BorderPane.setAlignment(display,Pos.CENTER);
    }

    /**
     * This Runnable updates the display of the Gui, but it checks to make
     * sure the Auction House successfully registered with the bank.
     */
    private Runnable uiUpdater = () ->{
        Runnable updater = this::update;
        if(auction.checkRegistration()){
            connect.setDisable(true);
            System.out.println("connection worked");
            catalogue = auction.getCatalogue();
            log = auction.getLog();
            id.setText("ID: "+ auction.getShortId(auction.getAuctionId()));
            while(!done){
                Platform.runLater(updater);
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::finish);
        }else{
            System.out.println("connection failed");
        }
    };

    /**
     * used to keep track of what index the vLog would be at if it was a List
     */
    private int displayIndex = 0;
    /**
     * looping method that updates the display with the current information.
     * In this case it updates the catalogue, balance, and log.
     */
    private void update(){
        auction.getBankBalance();
        listDisplay.getChildren().clear();
        int size = log.size();
        if(displayIndex < size){
            for(int i = displayIndex;i < size; i++){
                Text temp = new Text(log.get(i));
                vLog.getChildren().add(temp);
            }
            displayIndex = size;
        }
        boolean noBidding = updateCatalogue();
        balance.setText("Balance: "+auction.getBalance());
        disconnect.setDisable(noBidding);
    }

    /**
     * updates the display with the current catalogue (time left included).
     * Method also acts a way to check if there are any bidders, and
     * prevents shutdown if it finds any
     * @return returns true if a bidder was found, false otherwise
     */
    private Boolean updateCatalogue(){
        boolean noBidding = false;
        for (Item item : catalogue) {
            HBox guiItem = new HBox();
            if(item.getBidder() != null){
                noBidding = true;
            }
            String info = item.name();
            info = info.concat("         ");
            info = info + item.getCurrentBid();
            info = info.concat("         ");
            info = info + item.getTimeLeft();
            info = info.concat("         ");
            info = info + getStatus(item);
            Text name = new Text(info);
            guiItem.getChildren().add(name);
            listDisplay.getChildren().add(guiItem);
        }
        return noBidding;
    }

    /**
     * begins shutting down the auction house and prepares for a reset.
     */
    private void shutdown(){
        auction.shutdown();
        done = true;
        connect.setDisable(false);
        disconnect.setDisable(true);
    }

    /**
     * method to clean up the display after shutting down
     */
    private void finish(){
        listDisplay.getChildren().clear();
        log.clear();
        vLog.getChildren().clear();
        id.setText("ID: ");
        displayIndex = 0;
    }

    /**
     * Checks whether an agent bid on the item
     * @param item the item being check
     * @return true if there is a bidder, false otherwise
     */
    private String getStatus(Item item){
        if(item.getBidder() != null){
            return item.getBidderIdFour();
        }else{
            return "no bid";
        }
    }

    /**
     * creates and launches the guid
     * @param args string args(none needed)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
