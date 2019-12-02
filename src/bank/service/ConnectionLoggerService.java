package bank.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

/**
 * Singleton list to log connections and transactions to the server.
 */
public class ConnectionLoggerService {
    private final ObservableList<String> messageLog =
            FXCollections.observableArrayList();

    /**
     * Get this instance.
     *
     * @return ConnectionLoggerService
     */
    public static ConnectionLoggerService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Add a message to the messageLog.
     *
     * @param message String
     */
    public void add(String message) {
        Platform.runLater(() -> {
            messageLog.add(message);
        });
    }

    private static class InstanceHolder {
        private static final ConnectionLoggerService INSTANCE =
                new ConnectionLoggerService();
    }

    /**
     * Set the listView
     *
     * @param listView ListView<String></String>
     */
    public void setListView(ListView<String> listView) {
        listView.setItems(messageLog);
    }
}
