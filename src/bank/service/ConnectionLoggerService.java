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

    public static ConnectionLoggerService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void add(String message) {
        Platform.runLater(() -> {
            messageLog.add(message);
        });
    }

    private static class InstanceHolder {
        private static final ConnectionLoggerService INSTANCE =
                new ConnectionLoggerService();
    }

    public void setListView(ListView<String> listView) {
        listView.setItems(messageLog);
    }
}
