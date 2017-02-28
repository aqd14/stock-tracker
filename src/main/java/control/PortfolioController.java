package main.java.control;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import main.java.model.Stock;

public class PortfolioController extends ParentController implements Initializable {
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	
	int rowsPerPage = 10;
	TableView<Stock> table;
	List<Stock> stocks;
	
	public PortfolioController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Initialize portfolio when
	 */
	public void initPortfolio() {
		if (user != null) {
			stocks = userStockManager.findStocks(user.getId());
			portfolioPagination.setPageCount(stocks.size()/rowsPerPage + 1);
			table = createTable();
			portfolioPagination.setPageFactory(this::createPage);
		}
	}
	
	public void initTransactionHistory() {
		if (user != null) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private TableView<Stock> createTable() {

		TableView<Stock> table = new TableView<>();

		TableColumn<Stock, String> stockCodeCol = new TableColumn<>("Stock Code");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
		stockCodeCol.setPrefWidth(80);

		TableColumn<Stock, String> stockNameCol = new TableColumn<>("Company");
		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockName()));
		stockNameCol.setPrefWidth(250);

		TableColumn<Stock, String> stockPriceCol = new TableColumn<>("Bought Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPriceString()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<Stock, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getAmount())));
		amountCol.setPrefWidth(100);

		table.getColumns().addAll(stockCodeCol, stockNameCol, stockPriceCol, amountCol);
		return table;
	}
	
	private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, stocks.size());
        table.setItems(FXCollections.observableArrayList(stocks.subList(fromIndex, toIndex)));
        return new BorderPane(table);
    }
}
