package shared;

import AuctionHouse.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Steven Chase
 * messaging system between an agent and an Auction House
 */
public class AuctionMessage implements Serializable {
    private AMType type;   //what kind of message is it
    private ArrayList<Item> catalogue; //list of items for sale
    private Double amount;          //amount to bid
    private UUID itemID;       //The item you want to bid on
    private UUID accountId; //ID of client when first contacting Auction
    private String name; //name of the item

    /**
     * Enums to let the Agent/Auction House know the topic when
     * sending/receiving messages.
     */
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

    /**
     * Builder pattern for AuctionMessage
     */
    public static class Builder {
        private AMType type = null;
        private ArrayList<Item> catalogue = null;
        private Double amount = null;
        private UUID itemId = null;
        private UUID accountId = null;
        private String name = null;

        /**
         * sets the type of the message
         * @param type the type being set
         * @return returns the builder
         */
        public Builder type(AMType type) {
            this.type = type;
            return this;
        }

        /**
         * sets the amount of the message
         * @param amount the amount being set
         * @return returns the builder
         */
        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * sets the catalogue of the message
         * @param catalogue the catalogue(List) being set
         * @return returns the builder
         */
        public Builder list(ArrayList<Item> catalogue) {
            this.catalogue = catalogue;
            return this;
        }

        /**
         * sets the itemID of the message
         * @param itemID the UUID being set
         * @return returns the builder
         */
        public Builder item(UUID itemID) {
            this.itemId = itemID;
            return this;
        }

        /**
         * sets the accountId variable
         * @param accountId the UUID being set
         * @return returns the builder
         */
        public Builder id(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        /**
         * sets the amount of the message
         * @param amount the amount being set
         * @return returns the builder
         */
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * sets the name of the message
         * @param name The String being set
         * @return returns the builder
         */
        public Builder name(String name){
            this.name = name;
            return this;
        }

        /**
         * creates a builder
         * @return returns the created builder
         */
        public static Builder newB(){
            return new Builder();
        }

        /**
         * convert the builder into an AuctionMessage
         * @return returns a AuctionMessage representation of a Builder
         */
        public AuctionMessage build(){
            return new AuctionMessage(this);
        }
    }

    /**
     * Constructor for AuctionMessage using the builder
     * @param builder builder object used for AuctionMessage construction.
     */
    public AuctionMessage(Builder builder) {
        this.type = builder.type;
        this.catalogue = builder.catalogue;
        this.amount = builder.amount;
        this.itemID = builder.itemId;
        this.accountId = builder.accountId;
        this.name = builder.name;
    }

    /**
     * gets the AMType of the message
     * @return returns the AMType type
     */
    public AMType getType(){
        return type;
    }

    /**
     * gets the catalogue of items for sale
     * @return returns the catalogue sent by the Auction House
     */
    public ArrayList getCatalogue(){
        return catalogue;
    }

    /**
     * gets the amount
     * @return returns the amount parameter
     */
    public Double getAmount(){
        return amount;
    }

    /**
     * Gets the itemID from a message
     * @return returns the itemID parameter
     */
    public UUID getItem(){
        return itemID;
    }

    /**
     * @return returns UUID of sender
     */
    public UUID getId(){
        return accountId;
    }

    /**
     * gets the name from the message
     * @return the name of item of discussion(bidding, rejection, won, etc.)
     */
    public String getName(){
        return  name;
    }

    /**
     * Displays contents of message as a string
     * @return returns the string representation of a message
     */
    @Override
    public String toString() {
        String message = "{ ";
        if(type != null){
            message = message + type;
        }
        if(catalogue != null){
            message = message + ", "+catalogue;
        }
        if(amount != null){
            message = message+", "+amount;
        }
        if(itemID != null){
            message = message+", "+itemID;
        }
        if(accountId != null){
            message = message+","+accountId;
        }
        message = message + " }";
        return message;
    }
}