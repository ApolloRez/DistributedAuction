package AgentBackup;

import AgentBackup.Client.AuctionProxy;
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
import java.util.List;

/**
 * @author Steven Chase
 * This class is dedicated to display the information of an Agent object, and
 * it displays the information of any auction the Agent object is connected to.
 * The GUI can switch between any currently connected auction and display
 * its contents.
 * Please note: Due to the reason this class was made. This class is very
 * similar to AuctionGui in the AuctionHouse package since I used AuctionGui
 * as a foundation when writing this class.
 */
public class ClientGui extends Application {
    /**
     * bPane: BorderPane holding everything.
     * ipInputField: TextField to input ip of bank
     * portInput: TextField to input port number of bank
     * agent: The Agent object this class is displaying/interacting with
     * disconnect: Button to shutdown the program
     * connect: button to connect to the bank with the provided ip and port
     * log: the log in Agent agent to display in the GUI
     * vLog: VBox to display log
     * auctionList: VBox that holds buttons to click to display an auction
     * catalogueView: VBox used to display the contents on a specific
     * AuctionHouse.
     * activeAuctions: List of currently connected AuctionHouses provided
     * by agent.
     * auctionView: List of buttons set to display a specific AuctionHouse's
     * contents.
     * refresh: button to send a message to bank to request a current list
     * of connected AuctionHouses
     * done: boolean to shutdown any looping Threads (uiUpdater)
     */
    private BorderPane bPane = new BorderPane();
    private TextField ipInputField = new TextField("64.106.20.214");
    private TextField portInput = new TextField("4444");
    private Client agent;
    private Button disconnect = new Button("Shutdown");
    private Button connect = new Button("connect");
    private ArrayList<String> log = new ArrayList<>();
    private VBox vLog = new VBox();
    private VBox auctionList = new VBox();
    private VBox catalogueView = new VBox();
    private List<AuctionProxy> activeAuctions;
    private List<AuctionView> auctionView = new ArrayList<>();
    private Button refresh = new Button("Refresh");
    private boolean done = false;


    /**
     * creates the Gui and calls and setup methods for further initialization.
     * Also sets the closeRequest to refuse if the agent is currently bidding.
     * @param stage The stage
     */
    @Override
    public void start(Stage stage) {
        setupTopWindow();
        setupLeftWindow();
        setupLog();
        EventHandler<ActionEvent> grabNetInfo = e -> refreshList();
        refresh.setDisable(true);
        refresh.setOnAction(grabNetInfo);
        Scene scene = new Scene(bPane,600,600);
        stage.setOnCloseRequest(e -> {
            e.consume();
            if(!isBid){
                Platform.exit();
                System.exit(0);
            }else{
                log.add("Bidding in progress. Cannot exit");
            }
        });
        setupCenterWindow();
        EventHandler<ActionEvent> event = e -> shutdown();
        disconnect.setOnAction(event);
        disconnect.setDisable(true);
        catalogueView.setAlignment(Pos.CENTER);
        VBox left = new VBox();
        left.setAlignment(Pos.CENTER);
        left.getChildren().add(auctionList);
        bPane.setRight(left);
        stage.setTitle("Agent");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Setups up the center of bPane to show any AuctionHouse's contents.
     */
    private void setupCenterWindow(){
        ScrollPane display = new ScrollPane();
        Insets inset = new Insets(10);
        bPane.setCenter(display);
        BorderPane.setMargin(display,inset);
        BorderPane.setAlignment(display,Pos.CENTER);
        display.setContent(catalogueView);
    }

    /**
     * Setups the top of bPane with the TextField inputs and buttons to
     * connect/disconnect with the Bank
     */
    public void setupTopWindow(){
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

        EventHandler<ActionEvent> event = e -> createAgent();
        connect.setOnAction(event);
        topWindow.getChildren().add(connect);
        topWindow.getChildren().add(disconnect);
        topWindow.getChildren().add(refresh);
        topWindow.setSpacing(10);
        bPane.setTop(topWindow);
    }

    /**
     * agentId: four letter length of agent's UUID
     * bankBalance: available "money" the agent can spend
     * reservedBalance: "money" held by the bank due to a bid
     * SetupLeftWindow setups the Texts mentioned above and adds it to BPane's
     * left window
     */
    private Text agentId;
    private Text bankBalance;
    private Text reservedBalance;
    private void setupLeftWindow(){
        VBox left = new VBox();
        agentId = new Text();
        bankBalance = new Text("Available: ");
        reservedBalance = new Text("In Auction: ");
        left.getChildren().add(agentId);
        left.getChildren().add(bankBalance);
        left.getChildren().add(reservedBalance);
        left.setSpacing(15);
        left.setAlignment(Pos.TOP_LEFT);
        bPane.setLeft(left);
    }

    /**
     * Setups vLog to display log and adds it to bPane's bottom window
     */
    public void setupLog(){
        ScrollPane logDisplay = new ScrollPane();
        logDisplay.setPrefViewportHeight(150);
        logDisplay.setFitToWidth(true);
        logDisplay.setContent(vLog);
        logDisplay.vvalueProperty().bind(vLog.heightProperty());
        bPane.setBottom(logDisplay);
    }

    /**
     * signals the agent to terminate any of its threads, close all sockets,
     * and then terminate itself. The method then changes some variables
     * so another Agent can be created if desired.
     */
    private void shutdown(){
        refresh.setDisable(true);
        agent.shutdown();
        done = true;
        connect.setDisable(false);
        disconnect.setDisable(true);
    }

    /**
     * createAgent creates an agent(ensuring there are any port number errors)
     * and starts the uiUpdater Thread.
     */
    private void createAgent(){
        done = false;
        vLog.getChildren().clear();
        String bankIp = ipInputField.getText();
        int bankPort;
        try{
            bankPort = Integer.parseInt(portInput.getText());
        }catch(NumberFormatException e){
            Text text = new Text("bank port input error");
            vLog.getChildren().add(text);
            return;
        }
        agent = new Client(bankIp,bankPort);
        Thread thread = new Thread(uiUpdater);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * The Thread first checks to make sure the agent was able to connect
     * successfully with the Bank.If it doesn't, the Thread terminates itself.
     * If it does, the Thread continues and displays the agent's information.
     * After that, the Thread begins looping to update the GUI every second.
     */
    private Runnable uiUpdater = () ->{
        if(agent.checkRegistration()){
            agentId.setText("ID: "+agent.getShortId());
            connect.setDisable(true);
            refresh.setDisable(false);
            disconnect.setDisable(false);
            log = agent.getLog();
            activeAuctions = agent.getActiveAuctions();
            while(!done){
                Platform.runLater(this::update);
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::finish);
        }else{
            Platform.runLater(this::failed);
        }
    };

    /**
     * This method updates log, catalogueView, AuctionList, bankBalance, and
     * reservedBalance with latest information provided by the Agent and
     * AuctionHouses.
     */
    private int displayIndex = 0;
    private void update(){
        updateAuctionList();
        int size = log.size();
        if(displayIndex < size){
            for(int i = displayIndex;i < size; i++){
                Text temp = new Text(log.get(i));
                vLog.getChildren().add(temp);
            }
            displayIndex = size;
        }
        bankBalance.setText("Available: "+agent.getBankBalance());
        reservedBalance.setText("In Auction: "+agent.getReservedBalance());
    }

    /**
     * This method cleans auctionView and catalogueView if an AuctionHouse
     * disconnects, or it updates auctionView with latest information.
     */
    private boolean isBid = false;
    private void updateAuctionList(){
        activeAuctions = agent.getActiveAuctions();
        if(auctionView.size() != activeAuctions.size()){
            auctionList.getChildren().clear();
            catalogueView.getChildren().clear();
            auctionView.clear();
            for (AuctionProxy activeAuction : activeAuctions) {
                if (activeAuction.getShortAuctionId() == null) {
                    agent.getOneRegCheck();
                }
            }
            for(AuctionProxy auction: activeAuctions){
                AuctionView temp = new AuctionView(auction);
                auctionView.add(temp);
            }
            setupAuctionView();
        }else{
            isBid = false;
            for(int i =0; i < auctionView.size();i++){
                AuctionView view = auctionView.get(i);
                AuctionProxy auction = activeAuctions.get(i);
                view.readIn(auction);
                if(view.isBid()){
                    isBid = true;
                }
            }
            disconnect.setDisable(isBid);
        }
    }

    /**
     * This method lets the User know if any errors occurred when
     * creating the Agent, specifically when creating the socket.
     */
    private void failed(){
        vLog.getChildren().add(new Text("connection failed"));
    }

    /**
     * Setups initial AuctionView.
     */
    private void setupAuctionView(){
        for(AuctionView auction: auctionView){
            EventHandler<ActionEvent> display = e ->
                    displayCatalogue(auction.getCatalogueView());
            Button button = auction.getButton();
            button.setOnAction(display);
            auctionList.getChildren().add(button);
        }
    }

    /**
     * When an AuctionView's selectAuction button  is pressed on the right
     * window of bPane, this method displays the AuctionView's content.
     * @param view The VBox catalogueView of an AuctionView.
     */
    private void displayCatalogue(VBox view){
        catalogueView.getChildren().clear();
        catalogueView.getChildren().add(view);
    }

    /**
     * requests the agent to ask the Bank for an updated List of currently
     * connected AuctionHouses
     */
    public void refreshList(){
        agent.getNetInfo();
    }
    /**
     * method to clean up the display after shutting down
     */
    private void finish(){
        catalogueView.getChildren().clear();
        log.clear();
        vLog.getChildren().clear();
        auctionList.getChildren().clear();
        agentId.setText("ID: ");
        bankBalance.setText("Available: ");
        reservedBalance.setText("In Auction: ");
        displayIndex = 0;
    }
}
