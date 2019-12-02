package AuctionHouse;

import java.util.*;

public class Item {
    private String name;
    private double currentBid;
    private UUID bidderId;
    private UUID houseID;
    private UUID itemID;
    private double minimumBid;

    public String name(){
        return name;
    }

    public double getCurrentBid(){
        return currentBid;
    }

    public double getMinimumBid(){
        return minimumBid;
    }

    public void outBid(UUID bidder, double amount){
        this.bidderId = bidder;
        this.currentBid = amount;
    }

    public UUID getHouseID(){
        return houseID;
    }
    public UUID getItemID(){
        return itemID;
    }

    public UUID getBidder(){
        return bidderId;
    }

    public Item(String name, double value,UUID houseID){
        this.minimumBid = value;
        this.name = name;
        this.houseID = houseID;
        itemID = UUID.randomUUID();
    }
}
