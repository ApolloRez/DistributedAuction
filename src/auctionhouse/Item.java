package auctionhouse;

import java.io.Serializable;
import java.util.UUID;

public class Item implements Serializable {
    private final String name;  //Name of the item
    private double currentBid; //Double value needed to bid
    private UUID bidderId; //The UUID of the current agent bidder
    private final UUID houseID; //UUID of the auction that created the item
    private final UUID itemID; //UUID of the item
    private final double minimumBid; //Initial bid value
    private long timeLeft; //Time left till bid ends(in this case 0)
    private long bidTime; //The timestamp of the latest bid

    /**
     * constructor for an Item
     *
     * @param name    name of item
     * @param value   Initial value for bidding
     * @param houseID The UUID of the auction house that created it
     */
    public Item(String name, double value, UUID houseID) {
        this.minimumBid = value;
        this.currentBid = this.minimumBid;
        this.name = name;
        this.houseID = houseID;
        bidderId = null;
        timeLeft = 30;
        itemID = UUID.randomUUID();
        bidTime = System.currentTimeMillis();
    }

    /**
     * @return returns name parameter
     */
    public String name(){
        return name;
    }

    /**
     * @return returns the currentBid parameter
     */
    public double getCurrentBid(){
        return currentBid;
    }

    /**
     * @return returns minimumBid parameter
     */
    public double getMinimumBid(){
        return minimumBid;
    }

    /**
     * Method to replace the old bidder/value with the new bidder/value
     * @param bidder the new bidder(Agent)
     * @param amount the new amount(bid value)
     */
    public void outBid(UUID bidder, double amount){
        this.bidderId = bidder;
        this.currentBid = amount;
        reset();
    }

    /**
     * This method replaces the bidTime with the current time. Called when
     * there's an outbid
     */
    public void reset(){
        bidTime = System.currentTimeMillis();
    }

    /**
     * calculates the time elapsed based on currentTime and bidTime. timeLeft
     * is updated with the time elasped
     * @param currentTime the current time
     */
    public void updateTimer(long currentTime){
        timeLeft = 30 - ((currentTime-bidTime)/1000);
    }

    /**
     * gets timeLeft
     * @return returns the timeLeft parameter
     */
    public long getTimeLeft(){
        return timeLeft;
    }

    /**
     * @return returns the itemID parameter
     */
    public UUID getItemID(){
        return itemID;
    }

    /**
     * @return returns the bidderId parameter
     */
    public UUID getBidder(){
        return bidderId;
    }

    /**
     * This method grabs the first four digits of itemID. Used for display
     * purposes
     * @return returns a string of the first 4 digits of itemID
     */
    public String getBidderIdFour(){
        String id = bidderId.toString();
        String shortened = "";
        for(int i = 0; i < 4;i++){
            shortened = shortened.concat(String.valueOf(id.charAt(i)));
        }
        return shortened;
    }
}
