package shared;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private MessegeType messegeType;
    public Message(String message) {
        this.message = message;
    }
}
