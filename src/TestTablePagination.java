import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TestTablePagination extends Application {

    private final static int dataSize = 10_023;
    private final static int rowsPerPage = 1000;

    private final TableView<Sample> table = createTable();
    private final List<Sample> data = createData();

    private List<Sample> createData() {
        List<Sample> data = new ArrayList<>(dataSize);

        for (int i = 0; i < dataSize; i++) {
            data.add(new Sample(i, "foo " + i, "bar " + i));
        }

        return data;
    }

    private TableView<Sample> createTable() {

        TableView<Sample> table = new TableView<>();

        TableColumn<Sample, Integer> column1 = new TableColumn<>("Id");
        column1.setCellValueFactory(param -> param.getValue().id);
        column1.setPrefWidth(150);

        TableColumn<Sample, String> column2 = new TableColumn<>("Foo");
        column2.setCellValueFactory(param -> param.getValue().foo);
        column2.setPrefWidth(250);

        TableColumn<Sample, String> column3 = new TableColumn<>("Bar");
        column3.setCellValueFactory(param -> param.getValue().bar);
        column3.setPrefWidth(250);

        table.getColumns().addAll(column1, column2, column3);

        return table;
    }

    private Node createPage(int pageIndex) {

        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, data.size());
        table.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

        return new BorderPane(table);
    }

    @Override
    public void start(final Stage stage) throws Exception {

        Pagination pagination = new Pagination((data.size() / rowsPerPage + 1), 0);
        pagination.setPageFactory(this::createPage);

        Scene scene = new Scene(new BorderPane(pagination), 1024, 768);
        stage.setScene(scene);
        stage.setTitle("Table pager");
        stage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static class Sample {

        private final ObservableValue<Integer> id;
        private final SimpleStringProperty foo;
        private final SimpleStringProperty bar;

        private Sample(int id, String foo, String bar) {
            this.id = new SimpleObjectProperty<>(id);
            this.foo = new SimpleStringProperty(foo);
            this.bar = new SimpleStringProperty(bar);
        }
    }
}