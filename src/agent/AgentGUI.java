/**
 * @author Aidan O'Hara, Magnus Lindland, Steven Chase
 * Dec-19
 * Distributed Agent
 */

package agent;

import auctionhouse.Item;
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
import shared.NetInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentGUI extends Application {
    /**
     * bPane - the general border Pane
     * agent - the agent object
     * bankIPString - the bank's ip string
     * bankPortNumber - the bank's port number (4444)
     * connect - connect button to connect to the bank
     * disconnect - shutdown button for the bank
     * balance - text so the user can see their full balance
     * available - text so the user can see their available balance
     * aHLBox - vBox for displaying the auctionHouses
     * itemBox - vBox for displaying the items up for bid
     * agentWindow - hBox for storing the bidLog and the balances
     * bidLog - displays the agent's bidding status
     * lastBidLog - use this string to make sure the bid log doesn't spam the display
     * catalogue - locally stored catalogue of items from the auction house
     * auctionHouses - locally stored list of auctionHouses netInfo
     * runningLists - a bool to control the updater
     * ipInputField - user input's ip string
     * portInput - user input's port number
     * aHLInput - the user's auction house choice
     * itemChoice - the user's choice for item to bid on
     * bidAmount - user input for bid amount
     * depositAmount - user input for the deposit amount
     * logDisplay - ScrollPane for bidLog
     */
    BorderPane bPane = new BorderPane();

    private Agent agent;
    private String bankIPString;
    private int bankPortNumber;

    private final Button connect = new Button("connect");
    private final Button disconnect = new Button("Shutdown");
    private final Text balance = new Text("balance: ");
    private final Text available = new Text("available: ");
    private final VBox aHLBox = new VBox();
    private final VBox itemBox = new VBox();
    private final HBox agentWindow = new HBox();
    private final VBox bidLog = new VBox();

    private String lastBidLog;

    private ArrayList<Item> catalogue = new ArrayList<>();
    private List<NetInfo> auctionHouses;

    private boolean runningLists;

    private final TextField ipInputField = new TextField("ip String");
    private final TextField portInput = new TextField("portNumber int");
    private final TextField aHLInput = new TextField("choice");
    private final TextField itemChoice = new TextField("itemChoice");
    private final TextField bidAmount = new TextField("bid Amount");
    private final TextField depositAmount = new TextField("deposit Amount");

    private ScrollPane logDisplay;


    /**
     * starts the program
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        agentWindowSetup();
        connectToBankWindow();
        auctionHousesWindow();
        biddingWindowSetup();
        Scene scene = new Scene(bPane, 800,600);
        primaryStage.setOnCloseRequest(e -> {
            if (agent != null) {
                if (agent.getActiveBid()) {
                    e.consume();
                }
            } else {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.setTitle("Distributed Agent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * sets up the center to display items from the auction house
     */
    private void biddingWindowSetup() {
        ScrollPane itemDisplay = new ScrollPane();
        itemBox.setSpacing(5);
        itemBox.setAlignment(Pos.CENTER);
        itemDisplay.setContent(itemBox);
        bPane.setCenter(itemDisplay);
    }

    /**
     * creates the space to display the auction houses net info as well as
     *     buttons and text-fields allowing the user to connect/disconnect
     *     from the auction house as well as bid, and deposit funds to the
     *     bank.
     */
    private void auctionHousesWindow() {
        ScrollPane display = new ScrollPane();
        display.setMinSize(150,100);
        aHLBox.setAlignment(Pos.CENTER);
        aHLBox.setMaxWidth(50);
        display.setContent(aHLBox);
        bPane.setLeft(display);

        Button connectAH = new Button("Connect");
        EventHandler<ActionEvent> connect = e-> {
            int choice = Integer.parseInt(aHLInput.getText());
            agent.connectToAuctionHouse(choice - 1);
        };
        connectAH.setOnAction(connect);

        Button disconnectAH = new Button("Shutdown");
        EventHandler<ActionEvent> discEvent = e-> {
            if (!agent.getActiveBid()) {
                if (agent.getConnectedToAH()) {
                    shutDownAH();
                }
            }
        };
        disconnectAH.setOnAction(discEvent);

        VBox aHLChoice = new VBox();
        aHLChoice.setAlignment(Pos.CENTER);
        aHLChoice.minWidth(100);
        Text choiceLabel = new Text("Auction House Choice");
        aHLInput.setMaxSize(50,50);

        Button refreshAHList = new Button("refresh");
        EventHandler<ActionEvent> refresh = e-> {
            updateBalances();
            updateAHList();
        };
        refreshAHList.setOnAction(refresh);

        aHLChoice.getChildren().addAll(choiceLabel,aHLInput,connectAH,
                refreshAHList, disconnectAH);

        Button deposit = new Button("Deposit");
        EventHandler<ActionEvent> event = e -> {
            try {
                double depositIn = Double.parseDouble(depositAmount.getText());
                agent.bankDeposit(depositIn);
            } catch (NumberFormatException ex2) {
                ex2.printStackTrace();
            }
        };
        deposit.setOnAction(event);

        Text depositInfo = new Text("Deposit Amount");
        aHLChoice.getChildren().addAll(depositInfo,depositAmount,deposit);
        Text bidInfo = new Text("Bid:");
        Button bidButton = new Button("send Bid");

        EventHandler<ActionEvent> bid = e -> {
            try {
                double bidInput = Double.parseDouble(bidAmount.getText());
                int choiceInput = Integer.parseInt(itemChoice.getText());
                agent.sendBidToAH(choiceInput - 1, bidInput);
            } catch (NumberFormatException | IOException nMF) {
                nMF.printStackTrace();
            }
        };
        bidButton.setOnAction(bid);

        Text bidAmountLabel = new Text("Bid Amount:");
        Text itemChoiceLabel = new Text("Item Choice: ");
        bidButton.setOnAction(bid);
        aHLChoice.getChildren().addAll(bidInfo,
                itemChoiceLabel,
                itemChoice,
                bidAmountLabel,
                bidAmount,
                bidButton);
        bPane.setRight(aHLChoice);
    }

    /**
     * this updates the AHList with new information as well as gets
     *     new info from the bank.
     */
    private void updateAHList() {
        aHLBox.getChildren().clear();
        if (auctionHouses != null) {
            int i = 0;
            for (NetInfo netInfo : auctionHouses) {
                i++;
                HBox guiItem = new HBox();
                Text info = new Text(i + " :" + netInfo.toString());
                guiItem.getChildren().add(info);
                aHLBox.getChildren().add(guiItem);
            }
            agent.requestAHList();
        }
    }

    /**
     * this updates the agent's balances, full and available.
     */
    private void updateBalances() {
        agentWindow.getChildren().clear();
        agentWindow.getChildren().add(logDisplay);
        agent.updateBalance();
        double newBalance  = agent.getBalance();
        HBox bItem = new HBox();
        Text newB = new Text("Balance: "+newBalance);
        bItem.getChildren().add(newB);

        double newABalance = agent.getAvailableBalance();
        HBox abItem = new HBox();
        Text newAB = new Text("Available: " + newABalance);
        abItem.getChildren().add(newAB);

        agentWindow.getChildren().addAll(bItem, abItem);
    }

    /**
     * de-registers from the current auction house and clears the item list.
     */
    private void shutDownAH() {
        agent.deRegisterAuctionHouse();
        itemBox.getChildren().clear();
    }


    /**
     * instantiates the bottom section of the display, 2 text fields
     *     and 2 buttons.
     */
    private void connectToBankWindow() {
        HBox connectToBank = new HBox();
        connectToBank.setMinHeight(100);
        connectToBank.setAlignment(Pos.CENTER);

        VBox bankIP = new VBox();
        Text ipText = new Text("Bank IP");
        bankIP.getChildren().addAll(ipText,ipInputField);
        connectToBank.getChildren().add(bankIP);

        VBox bankPort = new VBox();
        Text portText = new Text("Bank Port");
        portInput.setPrefWidth(60);
        bankPort.getChildren().addAll(portText,portInput);
        connectToBank.getChildren().add(bankPort);

        EventHandler<ActionEvent> connectEvent = e -> {
            bankIPString = ipInputField.getText();
            String portString = portInput.getText();
            bankPortNumber = Integer.parseInt(portString);
            createAgent();
        };
        connect.setOnAction(connectEvent);

        EventHandler<ActionEvent> closeEvent = e -> {
            if (!agent.getConnectedToAH() && !agent.getActiveBid()) {
                shutDown();
            }
        };
        disconnect.setOnAction(closeEvent);
        connectToBank.getChildren().add(connect);
        connectToBank.getChildren().add(disconnect);
        bPane.setBottom(connectToBank);
    }

    /**
     * has agent shut down with the bank, clears the AH List and sets the list
     *     running boolean to false.
     */
    private void shutDown() {
        agent.shutDownWithBank();
        aHLBox.getChildren().clear();
        runningLists = false;

    }

    /**
     * sets up the top section with display labels.
     */
    private void agentWindowSetup() {
        agentWindow.setMinHeight(100);
        agentWindow.setAlignment(Pos.CENTER);
        bidLogSetup();
        agentWindow.getChildren().add(balance);
        agentWindow.getChildren().add(available);
        agentWindow.setSpacing(20);
        bPane.setTop(agentWindow);
    }

    /**
     * sets up the scrollPane for the log and adds it to the agentWindow
     */
    private void bidLogSetup() {
        logDisplay = new ScrollPane();
        logDisplay.setMaxHeight(100);
        bidLog.setSpacing(5);
        bidLog.setAlignment(Pos.CENTER_LEFT);
        bidLog.setMinWidth(200);
        logDisplay.setContent(bidLog);
        logDisplay.setMinSize(500,100);
        agentWindow.getChildren().add(logDisplay);
    }

    /**
     * creates the agent object, requests netInfo and starts the updater thread.
     */
    private void createAgent() {
        agent = new Agent(bankIPString, bankPortNumber);
        agent.registerBank();
        agent.requestAHList();
        runningLists = true;
        lastBidLog = "";
        Thread thread = new Thread(listUpdater);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * keeps the gui up to date. namely the list of items and the log.
     */
    private final Runnable listUpdater = () -> {
        Runnable updater = this::update;
        if (agent.getConnectedToBank()) {
            if (agent.getConnectedToAH()) {
                catalogue = agent.getCatalogue();
            }
            auctionHouses = agent.getAuctionHouses();
            while (runningLists) {
                Platform.runLater(updater);
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::finish);
        } else {
            shutDownAH();
            shutDown();
        }
    };

    /**
     * clears the windows
     */
    private void finish() {
        aHLBox.getChildren().clear();
        itemBox.getChildren().clear();
    }

    /**
     * calls the actual update methods for the list and log
     */
    private void update() {
        itemBox.getChildren().clear();
        catalogue = agent.getCatalogue();
        auctionHouses = agent.getAuctionHouses();
        if (agent.getConnectedToAH()) {
            updateItemList();
            updateBidLog();
        }
    }

    /**
     * re-draws the items, with time, name, and current bid
     */
    private void updateItemList() {
        int i = 0;
        for (Item item : catalogue) {
            i++;
            HBox guiItem = new HBox();
            String info = i+": "+item.name();
            info = info.concat("         ");
            info = info + item.getCurrentBid();
            info = info.concat("         ");
            info = info + item.getTimeLeft();
            info = info.concat("         ");
            Text name = new Text(info);
            guiItem.getChildren().add(name);
            itemBox.getChildren().add(guiItem);
        }
        if (agent.getConnectedToAH()) {
            agent.getUpdatedCatalogue();
        }
    }

    /**
     * checks if the bidStatus is new, if it is it adds it to the log.
     */
    private void updateBidLog() {
        if (agent.getBidStatus() != lastBidLog) {
            VBox guiItem = new VBox();
            guiItem.getChildren().add(new Text(agent.getBidStatus()));
            bidLog.getChildren().add(guiItem);
            lastBidLog = agent.getBidStatus();
        }


    }


    /**
     * MAIN
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}


