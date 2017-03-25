package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.TransactionWrapper;
import main.java.model.UserStock;
import main.java.utility.AlertFactory;
import main.java.utility.Screen;
import main.java.utility.StageFactory;
import yahoofinance.YahooFinance;

public class PortfolioController extends BaseController implements Initializable, IController {
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	@FXML private JFXButton sellStockButton;
	
	final static int rowsPerPage = 10;
//	TableView<Stock> table;
//	List<Stock> stocks;
	
	// Currently owned stock table
	TableView<TransactionWrapper> portfolioTable;
	List<TransactionWrapper> portfolioTransactions;
	
	// Transaction history table
	TableView<TransactionWrapper> historyTable;
	List<TransactionWrapper> historyTransactions;
	
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
	 * Initialize portfolio
	 */
	public void initPortfolio() {
		if (user != null) {
//			stocks = userStockManager.findStocks(user.getId());
			portfolioTransactions = transactionManager.findTransactions(user.getId(), true);
//			portfolioPagination.setPageCount(stocks.size()/rowsPerPage + 1);
			portfolioPagination.setPageCount(portfolioTransactions.size()/rowsPerPage + 1);
//			table = createTable();
			portfolioTable = createPortfolioTable();
			portfolioPagination.setPageFactory(this::createPortfolioPage);
			// Sell stock when user click on button
			sellStockButton.setOnAction(event -> {
				// Only display alert when user select some stocks
				if (selectedStock != null && selectedStock.size() > 0) {
					Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.SELL_STOCK_SMS);
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						sellStock(selectedStock);
						// Refresh table view
						refreshTableView(selectedStock);
						// Clear selected stocl
						selectedStock.clear();
					} else {
						// User doesn't want to sell stock. Close alert and get back to portfolio page
					}
				} else {
					// Show warning when user didn't choose any stock to sell
					Alert alert = AlertFactory.generateAlert(AlertType.WARNING, CommonDefine.NOT_SELECT_ANY_STOCK_SMS);
					alert.showAndWait();
				}
			});
			// Open Stock Detail page when user select item on table view
			// TODO: Update portfolio view when user buy some stocks
			portfolioTable.setOnMouseClicked(eventHandler -> {
				if (2 == eventHandler.getClickCount()) {
					makeNewStage(Screen.STOCK_DETAILS, "Stock Details", "../view/StockDetails.fxml");
				}
			});
		}
	}
	
	/**
	 * Initialize transaction history.
	 * Need to consider dynamic or lazy in
	 */
	public void initTransactionHistory() {
		if (user != null) {
			historyTransactions = transactionManager.findTransactions(user.getId(), false);
			transactionHistoryPagination.setPageCount(historyTransactions.size()/rowsPerPage + 1);
			historyTable = createHistoryTable();
			transactionHistoryPagination.setPageFactory(this::createTransactionPage);
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
			return;
		}
		// Sell stock one by one
		double earnedAmount = 0;
		for (Stock soldStock : stocks) {
			// Get current price of stock
			try {
				yahoofinance.Stock yahooStock = yahoofinance.YahooFinance.get(soldStock.getStockCode());
				earnedAmount += yahooStock.getQuote().getPrice().doubleValue() * soldStock.getAmount();
				UserStock us = userStockManager.findUserStock(user.getId(), soldStock.getId());
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
	
	/**
	 * Refresh portfolio view when user sold stocks
	 * @param stock
	 */
	private void refreshTableView(List<Stock> stocks) {
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
				// Remove transactions from portfolio
				// Not so good using 2 nested loop
				// Think about another way
				for (Iterator<TransactionWrapper> iterator = portfolioTransactions.iterator(); iterator.hasNext();) {
					TransactionWrapper t = iterator.next();
					for (Stock s : stocks) {
						if (t.getStock().equals(s)) {
							portfolioTable.getItems().remove(t);
//							portfolioTransactions.remove(t);
						}
					}
				}
//			}
//		});
	}
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createPortfolioTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		table.setEditable(true);
		
		TableColumn<TransactionWrapper, String> transDateCol = new TableColumn<>("Date");
		transDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionDate()));
		transDateCol.setPrefWidth(90);
		
		TableColumn<TransactionWrapper, String> transTimeCol = new TableColumn<>("Time");
		transTimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionTime()));
		transTimeCol.setPrefWidth(90);

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
	
	@SuppressWarnings("unchecked")
	private TableView<TransactionWrapper> createHistoryTable() {

		TableView<TransactionWrapper> table = new TableView<>();
		table.setEditable(true);
		
		TableColumn<TransactionWrapper, String> transDateCol = new TableColumn<>("Date");
		transDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionDate()));
		transDateCol.setPrefWidth(90);
		
		TableColumn<TransactionWrapper, String> transTimeCol = new TableColumn<>("Time");
		transTimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionTime()));
		transTimeCol.setPrefWidth(90);

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
	
	private Node createPortfolioPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, portfolioTransactions.size());
        portfolioTable.setItems(FXCollections.observableArrayList(portfolioTransactions.subList(fromIndex, toIndex)));
        return new BorderPane(portfolioTable);
    }
	
	private Node createTransactionPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, historyTransactions.size());
        historyTable.setItems(FXCollections.observableArrayList(historyTransactions.subList(fromIndex, toIndex)));
        return new BorderPane(historyTable);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override public void makeNewStage(Screen target, String stageTitle, String url) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		switch(target) {
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				// Find selected stock in Java API
				TransactionWrapper item = portfolioTable.getSelectionModel().getSelectedItem();
				// TODO: Think about better way to find the stock given stock code
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(item.getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + item.getStockCode());
					e.printStackTrace();
					return;
				}
				stockController.setStock(yahooStock);
				stockController.updateStockData();
				break;
			default:
				return; // Move to undefined target. Should throw some exception?
		}
		Stage newStage = StageFactory.generateStage(stageTitle);
		newStage.setScene(new Scene(root));
		newStage.show();
	}

	@Override
	public void switchScreen(Screen target, String title, String url) {
		// TODO Auto-generated method stub
		
	}
}
