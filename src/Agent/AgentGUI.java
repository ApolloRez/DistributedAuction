package Agent;

import AuctionHouse.Item;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import shared.NetInfo;

import java.io.IOException;

public class AgentGUI extends Application {

    private String bankIPString;
    private int bankPortNumber;
    private Agent agent;
    private BorderPane bPane = new BorderPane();
    private BorderPane bankPane = new BorderPane();
    private BorderPane auctionPane = new BorderPane();
    private Scene bankMenuScene;
    private Scene auctionMenuScene;
    private Button connect = new Button("connect");
    private Button disconnect = new Button("Shutdown");
    private Stage stage;
    private boolean waitingToConnect = true;
    private Label balanceLabel;
    ListView<String> auctionHouses;

    ObservableList<String> houses = FXCollections.observableArrayList();



    private TextField ipInputField = new TextField("ip String");
    private TextField portInput = new TextField("portNumber int");


    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        netInfoWindow();
        Scene netInfoScene = new Scene(bPane, 400, 200);
        bankMenuScene = new Scene(bankPane, 500, 400);
        auctionMenuScene = new Scene(auctionPane, 300,500);
        stage.setTitle("Distributed Agent - Connect to Bank");
        stage.setScene(netInfoScene);
        stage.show();
    }


    private void netInfoWindow() {
        HBox netInfoWindow = new HBox();
        netInfoWindow.setAlignment(Pos.CENTER);
        VBox bankIP = new VBox();
        Text ipText = new Text("Bank IP");
        bankIP.getChildren().addAll(ipText,ipInputField);
        netInfoWindow.getChildren().add(bankIP);

        VBox bankPort = new VBox();
        Text portText = new Text("Bank Port");
        portInput.setPrefWidth(60);
        bankPort.getChildren().addAll(portText,portInput);
        netInfoWindow.getChildren().add(bankPort);

        EventHandler<ActionEvent> event = e -> {
            try {
                bankIPString = ipInputField.getText();
                System.out.println(bankIPString);
                String portString = portInput.getText();
                bankPortNumber = Integer.parseInt(portString);
                createAgent();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        };
        connect.setOnAction(event);

        netInfoWindow.getChildren().add(connect);
        bPane.setCenter(netInfoWindow);
    }

    private void createAgent() throws IOException, InterruptedException {
        agent = new Agent(bankIPString, bankPortNumber);
        agent.registerBank();
     //   if (agent.connectedToBank) {
     //       System.out.println("calling bankmenu");
            setBankMenuWindow();
       // }
        /*
        create the agent with the netinfo from before,
        have the agent register with bank, recall these are seprate

        once the bank has been succesfully connected too we need to change
            the scene to the bank menu.
            Bank Menu will need to have the following:
            - Auction House List
            - Deposit
            - Balance printout
            - Agent button, to make a pop up with   eh agent's UUID and items.
         */

    }


    // go through bank windown..
    /*
     - needs to be able to close safely
     - switch the balance label to a balance button
     - find a way to update the auction houses list consistently
     - look into the pop up widows so they aren't hovering labels
     -
     */
    private void setBankMenuWindow() throws IOException {
        VBox options = new VBox();
        options.setAlignment(Pos.CENTER_RIGHT);
        balanceLabel = new Label(getBalanceString());
        options.getChildren().add(balanceLabel);


        Button deposit = new Button("Deposit");
        EventHandler<ActionEvent> event = e -> {
            depositPopUp();
        };
        deposit.setOnAction(event);
        options.getChildren().add(deposit);

        Button info = new Button("About Agent");
        EventHandler<ActionEvent> about = e -> {
            aboutPopUp();
        };
        info.setOnAction(about);
        options.getChildren().add(info);

        EventHandler<ActionEvent> close = e -> {
            try {
                agent.shutDownWithBank();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            stage.setScene(auctionMenuScene);
        };
        disconnect.setOnAction(close);
        options.getChildren().add(disconnect);

        agent.requestAHList();
        VBox auctionList = new VBox();
        auctionList.setAlignment(Pos.CENTER_LEFT);
        auctionHouses = new ListView<>();
        if (agent.getAuctionHouses()!= null) {
            for (NetInfo netInfo : agent.getAuctionHouses()) {
                System.out.println(netInfo.toString());
                houses.add(netInfo.toString());
            }
        }
        houses.add("why");
        houses.add("wont");
        auctionHouses.getItems().addAll(houses);
        auctionHouses.setPrefHeight(300);
        auctionHouses.setMaxWidth(250);

        /*
        this thread may not be the way to go I dont know for sure though

         */

        Thread bankMenuUpdate = new Thread(new BankInfoUpdater(options, auctionList));
        bankMenuUpdate.start();




        Button select = new Button ("Connect to Selected");
        Button refresh = new Button("Refresh");
        EventHandler<ActionEvent> connectToAH = e -> {
            int choice = auctionHouses.getFocusModel().getFocusedIndex();
            try {
                agent.connectToAuctionHouse(choice);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (agent.getConnectedToAH()) {
                setAuctionHouseMenu();
            } else {
                try {
                    setBankMenuWindow();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        select.setOnAction(connectToAH);
        EventHandler<ActionEvent> refreshWindow = e -> {
            try {
                setBankMenuWindow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        refresh.setOnAction(refreshWindow);
        auctionList.getChildren().add(auctionHouses);
        auctionList.getChildren().add(refresh);
        auctionList.getChildren().add(select);
        bankPane.setLeft(auctionList);
        bankPane.setRight(options);
        stage.setScene(bankMenuScene);

    }

    public class BankInfoUpdater implements Runnable {
        private VBox options;
        private VBox auctionList;

        public BankInfoUpdater (VBox options, VBox auctionList) {
            this.options = options;
            this.auctionList = auctionList;
        }
        @Override
        public void run() {
            while(agent.run) {
                balanceLabel.setText(getBalanceString());
                options.layout();
                auctionList.setAlignment(Pos.CENTER_LEFT);
                if (agent.getAuctionHouses()!= null) {
                    for (NetInfo netInfo : agent.getAuctionHouses()) {
                        System.out.println(netInfo.toString());
                        if (!houses.contains(netInfo)) {
                            houses.add(netInfo.toString());
                        }
                    }
                }
            }
        }
    }




    private void setAuctionHouseMenu() {
        System.out.println("has AHMENU been called" );
        // just a list of items just like the auction houses list
        // a bid button and pop up for bidding
        //make sure there is a strong way to update it...
        VBox itemList = new VBox();
        itemList.setAlignment(Pos.CENTER);
        ListView<String> itemsList = new ListView<String>();
        ObservableList<String> itemStrings = FXCollections.observableArrayList();
        for (Item item : agent.getCatalogue()) {
            itemStrings.add(item.toString());
        }
        itemsList.getItems().addAll(itemStrings);
        itemList.setPrefHeight(300);
        itemList.setMaxWidth(250);
        Button bidButton = new Button("Bid");
        EventHandler<ActionEvent> bid = e -> {
            int itemChoice = itemsList.getFocusModel().getFocusedIndex();
            bidPopUp(agent.getCatalogue().get(itemChoice), itemChoice);
        };
        bidButton.setOnAction(bid);
        itemList.getChildren().add(itemsList);
        itemList.getChildren().add(bidButton);
        auctionPane.setCenter(itemList);
        stage.setScene(auctionMenuScene);
    }

    private void bidPopUp(Item item, int choice) {
        Popup bid = new Popup();
        bid.getContent().add(new Label("Item: "+item.toString()));
        TextField userBid = new TextField();
        Button bidButton = new Button("Bid");
        Button back = new Button("Cancel");
        EventHandler<ActionEvent> bidEvent = e -> {
            try {
                double amount = Double.parseDouble(userBid.getText());
                agent.sendBidToAH(choice,amount);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        EventHandler<ActionEvent> cancel = e -> {
            bid.hide();
        };
        bidButton.setOnAction(bidEvent);
        back.setOnAction(cancel);
        bid.getContent().add(userBid);
        bid.getContent().add(bidButton);
        bid.getContent().add(back);
        bid.show(stage);
    }

    private String getBalanceString() {
        return "Balance: "+agent.getBalance();
    }

    private void aboutPopUp() {
        Popup about = new Popup();
        about.getContent().add(new Label(agent.getAccountNumber().toString()));
        about.getContent().add(new Label(agent.getWonItems().toString()));
        Button closeButton = new Button("Close");
        EventHandler<ActionEvent> close = e -> {
            about.hide();
        };
        closeButton.setOnAction(close);
        about.getContent().add(closeButton);
        about.show(stage);
    }

    private void depositPopUp() {
        Popup makeADeposit = new Popup();
        VBox depositWindow = new VBox();
        depositWindow.getChildren().add(new Label("Enter Deposit Amount: "));
        TextField amount = new TextField();
        Button back = new Button("Cancel");
        Button submit = new Button ("Confirm");
        EventHandler<ActionEvent> cancel = e -> {
            makeADeposit.hide();
        };
        EventHandler<ActionEvent> confirm = e -> {
            try {
                Double deposit = Double.parseDouble(amount.getText());
                agent.bankDeposit(deposit);
                makeADeposit.hide();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        back.setOnAction(cancel);
        submit.setOnAction(confirm);
        depositWindow.getChildren().add(amount);
        depositWindow.getChildren().add(back);
        depositWindow.getChildren().add(submit);
        makeADeposit.getContent().add(depositWindow);
        makeADeposit.show(stage);
    }

//    public AuctionMenuUpdate implements Runnable {
//
  //  }

    public static void main(String args[]) {
        launch(args);
    }
}

