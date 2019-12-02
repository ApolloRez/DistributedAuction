package shared;

import AuctionHouse.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * messaging system between an agent and an Auction House
 */
public class AuctionMessage implements Serializable {
    private String notification;   //Auction letting client know status of bids
    private AMType type;   //what kind of message is it
    private ArrayList<Item> catalogue; //list of items for sale
    private Double amount;          //amount to bid
    private Item itemID;       //The item you want to bid on
    private UUID accountId; //ID of client when first contacting Auction House

    public enum AMType{
        BID,        //client wants to bid on item
        REGISTER,    //client first contacts Auction house/ Auction responds
        ACCEPTANCE, //Auction lets agent know their bid was accepted
        REJECTION,  //Auction lets agent know their bid was denied
        OUTBID,     //Auction lets agent know their bid was out bidded
        WINNER,     //Auction lets agent know they won bid
        UPDATE      //Auction sends updated catalogue to client
    }
    public static class Builder {
        private String notification = null;
        private AMType type = null;
        private ArrayList<Item> catalogue = null;
        private Double amount = null;
        private Item itemID= null;
        private UUID accountId = null;

        public Builder type(AMType type) {
            this.type = type;
            return this;
        }
        public Builder list(ArrayList<Item> catalogue){
            this.catalogue = catalogue;
            return this;
        }
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        public Builder item(Item itemID){
            this.itemID  = itemID;
            return this;
        }
        public Builder id(UUID accountId) {
            this.accountId = accountId;
            return this;
        }
        public static Builder newB(){
            return new Builder();
        }
        public AuctionMessage build(){
            return new AuctionMessage(this);
        }
    }

    public AuctionMessage(Builder builder) {
        this.notification = builder.notification;
        this.type = builder.type;
        this.catalogue = builder.catalogue;
        this.amount = builder.amount;
        this.itemID = builder.itemID;
        this.accountId = builder.accountId;
    }

    public String getNotification(){
        return notification;
    }

    public AMType getType(){
        return type;
    }

    public ArrayList getCatalogue(){
        return catalogue;
    }

    public Double getAmount(){
        return amount;
    }
    public Item getItem(){
        return itemID;
    }
    public UUID getId(){
        return accountId;
    }
}