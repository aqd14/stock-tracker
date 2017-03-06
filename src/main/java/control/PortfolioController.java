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
import main.java.model.TransactionWrapper;

public class PortfolioController extends ParentController implements Initializable {
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	
	int rowsPerPage = 10;
//	TableView<Stock> table;
//	List<Stock> stocks;
	
	TableView<TransactionWrapper> table;
	List<TransactionWrapper> transactions;
	
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
//			stocks = userStockManager.findStocks(user.getId());
			transactions = transactionManager.findTransactions(user.getId());
//			portfolioPagination.setPageCount(stocks.size()/rowsPerPage + 1);
			portfolioPagination.setPageCount(transactions.size()/rowsPerPage + 1);
//			table = createTable();
			table = createTable();
			portfolioPagination.setPageFactory(this::createPage);
		}
	}
	
	public void initTransactionHistory() {
		if (user != null) {
			
		}
	}
	
//	@SuppressWarnings("unchecked")
//	private TableView<Stock> createTable() {
//
//		TableView<Stock> table = new TableView<>();
//
//		TableColumn<Stock, String> stockCodeCol = new TableColumn<>("Stock Code");
//		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
//		stockCodeCol.setPrefWidth(80);
//
//		TableColumn<Stock, String> stockNameCol = new TableColumn<>("Company");
//		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockName()));
//		stockNameCol.setPrefWidth(250);
//
//		TableColumn<Stock, String> stockPriceCol = new TableColumn<>("Bought Price");
//		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPriceString()));
//		stockPriceCol.setPrefWidth(100);
//		
//		TableColumn<Stock, String> amountCol = new TableColumn<>("Quantity");
//		amountCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getAmount())));
//		amountCol.setPrefWidth(100);
//
//		table.getColumns().addAll(stockCodeCol, stockNameCol, stockPriceCol, amountCol);
//		return table;
//	}
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		
		TableColumn<TransactionWrapper, String> transDateCol = new TableColumn<>("Date");
		transDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionDate()));
		transDateCol.setPrefWidth(80);
		
		TableColumn<TransactionWrapper, String> transTimeCol = new TableColumn<>("Time");
		transTimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionTime()));
		transTimeCol.setPrefWidth(80);

		TableColumn<TransactionWrapper, String> stockCodeCol = new TableColumn<>("Stock Code");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCode()));
		stockCodeCol.setPrefWidth(80);

		TableColumn<TransactionWrapper, String> stockNameCol = new TableColumn<>("Company");
		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockCompany()));
		stockNameCol.setPrefWidth(250);

		TableColumn<TransactionWrapper, String> stockPriceCol = new TableColumn<>("Bought Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPrice()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAmount()));
		amountCol.setPrefWidth(100);

		table.getColumns().addAll(transDateCol,  transTimeCol, stockCodeCol, stockNameCol, stockPriceCol, amountCol);
		return table;
	}
	
	private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, transactions.size());
        table.setItems(FXCollections.observableArrayList(transactions.subList(fromIndex, toIndex)));
        return new BorderPane(table);
    }
}
