package AuctionHouse;

import java.util.Random;

public class Item {
    private String name;
    private int value;

    private String getName(){
        return name;
    }

    private int getValue(){
        return value;
    }

    private void setNewValue(int value){
        this.value = value;
    }

    public Item(int value, String name){
        Random random = new Random();
        this.value = value;
        this.name = name;
    }
}
