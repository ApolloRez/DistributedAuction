package Agent;

import AuctionHouse.Item;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    BorderPane bPane = new BorderPane();

    private Agent agent;
    private String bankIPString;
    private int bankPortNumber;

    private Button connect = new Button("connect");
    private Button disconnect = new Button("Shutdown");

    private Text balance = new Text("balance: ");
    private Text available = new Text("available: ");

    private VBox aHLBox = new VBox();
    private VBox itemBox = new VBox();
    private HBox agentWindow = new HBox();



    private ArrayList<Item> catalogue = new ArrayList<>();
    private List<NetInfo> auctionHouses;

    private boolean runningLists;

    private TextField ipInputField = new TextField("ip String");
    private TextField portInput = new TextField("portNumber int");
    private TextField aHLInput = new TextField("choice");
    private TextField itemChoice = new TextField("itemChoice");
    private TextField bidAmount = new TextField("bid Amount");
    private TextField depositAmount = new TextField("deposit Amount");


    @Override
    public void start(Stage primaryStage) throws Exception {
        agentWindowSetup();
        connectToBankWindow();
        auctionHousesWindow();
        biddingWindowSetup();
        Scene scene = new Scene(bPane, 800,600);
        primaryStage.setOnCloseRequest(e -> {
            if (agent.getActiveBid()) {
                System.out.println("Consume?");
                e.consume();
            } else {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.setTitle("Distributed Agent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void biddingWindowSetup() {
        ScrollPane itemDisplay = new ScrollPane();
        itemBox.setSpacing(5);
        itemBox.setAlignment(Pos.CENTER);
        itemDisplay.setContent(itemBox);
        bPane.setCenter(itemDisplay);
    }

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
            try {
                agent.connectToAuctionHouse(choice - 1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        connectAH.setOnAction(connect);

        Button disconnectAH = new Button("Shutdown");
        EventHandler<ActionEvent> discEvent = e-> {
            System.out.println(agent.getActiveBid());
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
            } catch (IOException | NumberFormatException ex2) {
                ex2.printStackTrace();
            }

        };
        deposit.setOnAction(event);
        Text depositInfo = new Text("Deposit Amount");
        aHLChoice.getChildren().addAll(depositInfo,depositAmount,deposit);

        //bid read in and bid button as well as item choice read in;
        Text bidInfo = new Text("Bid:");
        Button bidButton = new Button("send Bid");
        EventHandler<ActionEvent> bid = e -> {
            try {
                double bidInput = Double.parseDouble(bidAmount.getText());
                int choiceInput = Integer.parseInt(itemChoice.getText());
                agent.sendBidToAH(choiceInput - 1, bidInput);

            } catch (NumberFormatException | IOException nMF) {

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
            try {
                agent.requestAHList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBalances() {
        agentWindow.getChildren().clear();
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

    private void shutDownAH() {
        agent.deRegisterAuctionHouse();
        itemBox.getChildren().clear();
    }


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
            try {
                bankIPString = ipInputField.getText();
                System.out.println(bankIPString);
                String portString = portInput.getText();
                bankPortNumber = Integer.parseInt(portString);
                createAgent();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        connect.setOnAction(connectEvent);

        EventHandler<ActionEvent> closeEvent = e -> {
            System.out.println("clicked me");
            if (!agent.getConnectedToAH() && !agent.getActiveBid()) {
                shutDown();
            }
        };
        disconnect.setOnAction(closeEvent);
        connectToBank.getChildren().add(connect);
        connectToBank.getChildren().add(disconnect);
        bPane.setBottom(connectToBank);
    }

    private void shutDown() {
        agent.shutDownWithBank();
        aHLBox.getChildren().clear();
        runningLists = false;

    }


    private void agentWindowSetup() {
        agentWindow.setMinHeight(100);
        agentWindow.setAlignment(Pos.CENTER);
        agentWindow.getChildren().add(balance);
        agentWindow.getChildren().add(available);
        bPane.setTop(agentWindow);
    }



    private void createAgent() throws IOException {
        agent = new Agent(bankIPString, bankPortNumber);
        agent.registerBank();
        agent.requestAHList();
        runningLists = true;
        Thread thread = new Thread(listUpdater);
        thread.setDaemon(true);
        thread.start();

    }

    private Runnable listUpdater = () -> {
        Runnable updater = this::update;
        if (agent.getConnectedToBank()) {
            if (agent.getConnectedToAH()) {
                catalogue = agent.getCatalogue();
            }
            auctionHouses = agent.getAuctionHouses();
            while(runningLists){
                Platform.runLater(updater);
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::finish);
        } else {

        }
    };

    private void finish() {
        aHLBox.getChildren().clear();
        itemBox.getChildren().clear();
    }



    private void update() {
        itemBox.getChildren().clear();
        catalogue = agent.getCatalogue();
        auctionHouses = agent.getAuctionHouses();
        if (agent.getConnectedToAH()) {
            updateItemList();
        }
    }



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



    public static void main(String[] args) {
        launch(args);
    }
}


