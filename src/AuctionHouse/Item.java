package AuctionHouse;

import java.util.Random;
import java.util.UUID;

public class Item {
    private String name;
    private double value;
    private UUID bidderId;
    private int timeLeft;

    public String name(){
        return name;
    }

    public double value(){
        return value;
    }

    public void setNewValue(int value){
        this.value = value;
    }

    public UUID getBidder(){
        return bidderId;
    }

    public boolean over(){
        return timeLeft <= 0;
    }

    public void decrement(){
        timeLeft--;
    }
    public void slateForRemoval(){
        timeLeft = -1;
    }

    public Item(String name, double value){
        this.value = value;
        this.name = name;
        timeLeft = 30;
    }
}
