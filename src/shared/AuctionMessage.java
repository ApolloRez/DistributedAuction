package shared;

import AuctionHouse.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * messaging system between an agent and an Auction House
 */
public class AuctionMessage implements Serializable {
    private AMType type;   //what kind of message is it
    private ArrayList<Item> catalogue; //list of items for sale
    private Double amount;          //amount to bid
    private UUID itemID;       //The item you want to bid on
    private UUID accountId; //ID of client when first contacting Auction House

    public enum AMType{
        BID,        //client wants to bid on item
        REGISTER,    //client first contacts Auction house/ Auction responds
        ACCEPTANCE, //Auction lets agent know their bid was accepted
        REJECTION,  //Auction lets agent know their bid was denied
        OUTBID,     //Auction lets agent know their bid was out bidded
        WINNER,     //Auction lets agent know they won bid
        DEREGISTER, //client de-registers from auction house
        UPDATE      //Auction sends updated catalogue to client
    }
    public static class Builder {
        private AMType type = null;
        private ArrayList<Item> catalogue = null;
        private Double amount = null;
        private UUID itemID= null;
        private UUID accountId = null;

        public Builder type(AMType type) {
            this.type = type;
            return this;
        }

        public Builder list(ArrayList<Item> catalogue){
            this.catalogue = catalogue;
            return this;
        }
        public Builder item(UUID itemID){
            this.itemID  = itemID;
            return this;
        }
        public Builder id(UUID accountId) {
            this.accountId = accountId;
            return this;
        }
        public Builder amount(Double amount) {
            this.amount = amount;
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
        this.type = builder.type;
        this.catalogue = builder.catalogue;
        this.amount = builder.amount;
        this.itemID = builder.itemID;
        this.accountId = builder.accountId;
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
    public UUID getItem(){
        return itemID;
    }

    /**
     * @return returns UUID of sender
     */
    public UUID getId(){
        return accountId;
    }

    @Override
    public String toString() {
        return "AuctionMessage{" +
                "type=" + type +
                ", catalogue=" + catalogue +
                ", amount=" + amount +
                ", itemID=" + itemID +
                ", accountId=" + accountId +
                '}';
    }
}