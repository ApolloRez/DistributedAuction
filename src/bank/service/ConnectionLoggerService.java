package bank.service;

import javafx.beans.property.SimpleListProperty;
import shared.Message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionLoggerService {
    private final Queue<Message> messageLog = new ConcurrentLinkedQueue<>();
    private final SimpleListProperty<Message> messages =
            new SimpleListProperty<>();

    public static ConnectionLoggerService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public boolean add(Message message) {
        return messageLog.add(message);
    }

    private static class InstanceHolder {
        private static final ConnectionLoggerService INSTANCE =
                new ConnectionLoggerService();
    }
}
