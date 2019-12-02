package bank.gui;

import bank.service.ConnectionLoggerService;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

public class View {
    private StackPane root;
    private ConnectionLoggerService connectionLoggerService;
    private ListView<String> listView;

    public View() {
        connectionLoggerService = ConnectionLoggerService.getInstance();
        root = new StackPane();
        listView = new ListView<>();
        connectionLoggerService.setListView(listView);
        root.getChildren().add(listView);
    }
    public StackPane getRoot() {
        return root;
    }
}
