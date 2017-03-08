package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import main.java.model.Stock;
import main.java.model.TransactionWrapper;
import main.java.model.UserStock;

public class PortfolioController extends ParentController implements Initializable {
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	@FXML private JFXButton sellStockButton;
	
	int rowsPerPage = 10;
//	TableView<Stock> table;
//	List<Stock> stocks;
	
	TableView<TransactionWrapper> table;
	List<TransactionWrapper> transactions;
	
	// List of selected stocks that user consider to sell
	List<Stock> selectedStock = new ArrayList<>();
	
	public PortfolioController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Clear any selected stock when user open Portfolio
		// TODO: Consider switching tabs
		selectedStock.clear();
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
			// Sell stock when user click on button
			sellStockButton.setOnAction(event -> {
				sellStock(selectedStock);
			});
		}
	}
	
	/**
	 * <p>
	 * Sell owned stocks.
	 * Add up earned money to balance and remove UserStock instance from database.
	 * </p>
	 * 
	 * @param stocks List of selected stock in Portfolio
	 */
	private void sellStock(List<Stock> stocks) {
		if (null == stocks || stocks.size() <= 0) {
			// Show a dialog with error message for user
			return;
		}
		// Sell stock one by one
		double earnedAmount = 0;
		for (Stock stock : stocks) {
			// Get current price of stock
			try {
				yahoofinance.Stock yahooStock = yahoofinance.YahooFinance.get(stock.getStockCode());
				earnedAmount += yahooStock.getQuote().getPrice().doubleValue() * stock.getAmount();
				UserStock us = userStockManager.findUserStock(user.getId(), stock.getId());
				userStockManager.remove(us);
			} catch (IOException e) {
				System.err.println("Transaction failed. Rollback.");
				e.printStackTrace();
			}
		}
		// Add up value to user account
		double curBalance = user.getAccount().getBalance();
		user.getAccount().setBalance(curBalance + earnedAmount);
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
		table.setEditable(true);
		
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
		
		TableColumn<TransactionWrapper, Boolean> sellStockCol = new TableColumn<>();
		sellStockCol.setGraphic(new CheckBox());
		
		sellStockCol.setCellValueFactory(new PropertyValueFactory<TransactionWrapper, Boolean>("selected"));
		// Add event handler when users select a check box on table
		sellStockCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
		    @Override
		    public ObservableValue<Boolean> call(Integer index) {
		        TransactionWrapper item = table.getItems().get(index);
		        // Add/Remove item from the selected list
		        if (true == item.getSelected()) {
		            selectedStock.add(item.getStock());
		        } else {
		        	selectedStock.remove(item.getStock());
		        }
		        return item.selectedProperty();
		    }
		}));
		sellStockCol.setEditable(true);

		table.getColumns().addAll(sellStockCol, transDateCol,  transTimeCol, stockCodeCol, stockNameCol, stockPriceCol, amountCol);
		return table;
	}
	
	private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, transactions.size());
        table.setItems(FXCollections.observableArrayList(transactions.subList(fromIndex, toIndex)));
        return new BorderPane(table);
    }
}
