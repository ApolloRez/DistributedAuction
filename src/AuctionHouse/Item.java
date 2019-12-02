package AuctionHouse;

import java.util.*;

public class Item {
    private String name;
    private double currentBid;
    private UUID bidderId;
    private UUID houseID;
    private UUID itemID;
    private double minimumBid;
    private long timeLeft;
    private long bidTime;

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
        reset();
    }
    public void reset(){
        bidTime = System.currentTimeMillis();
    }
    public void updateTimer(long currentTime){
        timeLeft = 30 - ((currentTime-bidTime)/1000);
    }
    public long getTimeLeft(){
        return timeLeft;
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

    public long getBidTime(){
        return bidTime;
    }

    public Item(String name, double value,UUID houseID){
        this.minimumBid = value;
        this.currentBid = this.minimumBid;
        this.name = name;
        this.houseID = houseID;
        bidderId = null;
        timeLeft = 30;
        itemID = UUID.randomUUID();
        bidTime = System.currentTimeMillis();
    }
}
