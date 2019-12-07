package AgentBackup;
import AuctionHouse.Item;
import AgentBackup.Agent.AuctionProxy;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * In my AgentGui display, when you click on a button in the right window,
 * it displays a view of the Auction's items for sale. This class is used for
 * displaying an Auction's items for sale as well as being able to bid on
 * items.
 */
public class AuctionView {
    private AuctionProxy auction; //The AuctionHouse this class represents
    private ArrayList<Item> catalogue;//List of auction's items for sale
    private VBox catalogueView;//The VBox that holds everything
    private Button selectAuction;//The button to press to display catalogueView
    private List<ItemGui> items;//A list of ItemGuis representing the catalogue

    /**
     * This constructor creates intializes everything and sets auction.
     * It then calls setup.
     * @param auction The AuctionProxy this class will represent in AgentGui
     */
    public AuctionView(AuctionProxy auction){
        this.auction = auction;
        //The UUID of the Agent connected to auction via
        String shortId = auction.getShortAuctionId();
        catalogue = auction.getCatalogue();
        catalogueView = new VBox();
        selectAuction = new Button(shortId);
        items = new ArrayList<>();
        catalogueView.setSpacing(15);
        setup();
    }

    /**
     * This method creates the display that you would see when clicking
     * on Button selectAuction. It creates a header to help indicate what
     * auction you're viewing, and it then creates ItemGuis for each item
     * in the catalogue
     */
    private void setup(){
        catalogueView.getChildren().clear();
        String header = "Auction House: "+ auction.getShortAuctionId();
        Text id = new Text(header);
        catalogueView.getChildren().add(id);
        for(Item item: catalogue){
            ItemGui temp = new ItemGui(item);
            catalogueView.getChildren().add(temp.getBox());
            items.add(temp);
        }
    }

    /**
     * When a bid Button in a ItemGui is click, this is the method used to
     * bid. It grabs the TextField and the Item of the Button's ItemGui,
     * extracts the bid amount, and passes that information to AuctionHouse
     * for processing.
     * @param input The TextField with the bid amount
     * @param item The item being bidded on
     */
    private void bid(TextField input, Item item){
        String bidAmount = input.getText();
        input.clear();
        try{
            double amount = Double.parseDouble(bidAmount);
            auction.bid(amount,item.getItemID(),item.name());
        }catch(NullPointerException | NumberFormatException ignored){}
    }

    /**
     * This method is used for updating AuctionView's AuctionProxy auction
     * with the current information provided by the Agent
     * @param auction The latest status of an AuctionProxy
     */
    public void readIn(AuctionProxy auction){
        this.auction = auction;
        catalogue = auction.getCatalogue();
        update();
    }

    /**
     * this method goes through items and updates each ItemGui with the latest
     * information provided by the AuctionHouse.
     */
    private void update(){
        int index = 0;
        for(ItemGui item: items){
            if(index < catalogue.size()){
                item.update(catalogue.get(index));
                Button button = item.getButton();
                EventHandler<ActionEvent> bid = e ->
                        bid(item.getField(),item.item());
                button.setOnAction(bid);
                index++;
            }
        }
    }

    /**
     * gets Button selectAuction
     * @return returns AuctionView's Button selectAuction
     */
    public Button getButton(){
        return selectAuction;
    }

    /**
     * gets object's AuctionProxy
     * @return returns AuctionProxy auction
     */
    public AuctionProxy getAuction() {
        return auction;
    }

    /**
     * gets the object's VBox
     * @return returns the VBox catalogueView
     */
    public VBox getCatalogueView(){
        return catalogueView;
    }

    /**
     * This method goes through catalogue and checks if there are any bidders.
     * If finds and bidders, it returns true.
     * @return returns true if any Item in catalogue has a bidder, false
     * otherwise.
     */
    public boolean isBid(){
        for(int i =0;i < catalogue.size();i++){
            Item temp = catalogue.get(i);
            UUID bidder = temp.getBidder();
            if(bidder != null){
                return true;
            }
        }
        return false;
    }
}
