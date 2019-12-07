package AgentBackup;

import AuctionHouse.Item;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * @author Steven Chase
 * This class represents an auction item on sale. It displays the item name,
 * its price, time left, whether someone bid on it, TextField to enter your
 * bid, and a "bid" button to bid on that item
 */
public class ItemGui{
    private Item item; //The Item an ItemGui represents
    private Button bid;// bids on the item next to it
    private TextField bidAmount; //TextField to enter your bid price
    private HBox box;//holds all the Texts/TextField
    private Text name;//Text holding item name
    private Text amount;// Text hold item amount
    private Text timeLeft;//Text holding item timeLeft
    private Text status;//Text holding item status

    /**
     * Constructor that creates the displays showcasing an item with
     * the item provided.
     * @param item The item to display
     */
    public ItemGui(Item item){
        this.item = item;
        bid = new Button("Bid");
        bidAmount = new TextField();
        bidAmount.setPrefWidth(60);
        box = new HBox();
        name = new Text();
        amount = new Text();
        timeLeft = new Text();
        status = new Text();
        box.setSpacing(10);
        setup();
    }

    /**
     * This method sets the Texts with their appropriate Strings and
     * adds every Text/TextField to box for easy display.
     */
    private void setup(){
        String name = item.name();
        double amount = item.getCurrentBid();
        if(amount < item.getMinimumBid()){
            amount = item.getMinimumBid();
        }
        long timeLeft = item.getTimeLeft();
        box.getChildren().add(this.name);
        this.name.setText(name);
        box.getChildren().add(this.amount);
        this.amount.setText(String.valueOf(amount));
        box.getChildren().add(this.timeLeft);
        this.timeLeft.setText(String.valueOf(timeLeft));
        updatetStatus();
        box.getChildren().add(status);
        box.getChildren().add(bidAmount);
        box.getChildren().add(bid);
    }

    /**
     * This method updates ItemGui Texts with the new information provided
     * given by item.
     * @param item The item/info you want this ItemGui to display.
     */
    void update(Item item){
        this.item = item;
        String name = item.name();
        double amount = item.getCurrentBid();
        if(amount < item.getMinimumBid()){
            amount = item.getMinimumBid();
        }
        long timeLeft = item.getTimeLeft();
        this.name.setText(name);
        this.amount.setText(String.valueOf(amount));
        this.timeLeft.setText(String.valueOf(timeLeft));
        updatetStatus();
    }


    /**
     * gets the HBox box
     * @return returns the box HBox
     */
    public HBox getBox(){
        return box;
    }

    /**
     * gets the bid button
     * @return returns this ItemGui's bid Button.
     */
    public Button getButton(){
        return bid;
    }

    /**
     * gets the item Item
     * @return returns this ItemGui's item
     */
    public Item item(){
        return item;
    }

    /**
     * Checks if Item item has a bidder and displays the first 4 letters
     * of the bider's UUID. If there isn't a bidder, it displays "no bid"
     */
    private void updatetStatus(){
        if(item.getBidder() != null){
            status.setText(item.getBidderIdFour());
        }else{
            status.setText("no bid");
        }
    }

    /**
     * gets the TextField bidAmount
     * @return returns the TextField bidAmount
     */
    public TextField getField(){
        return bidAmount;
    }
}
