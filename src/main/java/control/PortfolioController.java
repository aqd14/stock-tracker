package main.java.control;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.TransactionWrapper;
import main.java.model.UserStock;
import main.java.utility.AlertFactory;
import main.java.utility.ExportUtils;
import main.java.utility.Screen;
import main.java.utility.StageFactory;
import yahoofinance.YahooFinance;

public class PortfolioController extends BaseController implements Initializable, IController {
	@FXML private AnchorPane mainAP;
	@FXML private TabPane mainTP;
	@FXML private Pagination portfolioPagination;
	@FXML private Pagination transactionHistoryPagination;
	@FXML private JFXButton sellStockButton;
	@FXML private JFXButton exportButton;
	
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
	List<TransactionWrapper> performingTransactions = new ArrayList<TransactionWrapper>();
	
	public PortfolioController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Clear any selected stock when user open Portfolio
		// TODO: Consider switching tabs
		performingTransactions.clear();
		
		// Resize tab's width based on selected tab
		// Need more width for Transaction History tab
		mainTP.getSelectionModel().selectedItemProperty().addListener(listener -> {
			int selectedIndex = mainTP.getSelectionModel().getSelectedIndex();
			System.out.println("Selected index: " + selectedIndex);
			double preferWidth;
			if (selectedIndex == 0) { // Portfolio view
				preferWidth = 760;
			} else {
				preferWidth = 950;
			}
			mainAP.getScene().getWindow().setWidth(preferWidth);
			mainAP.setPrefWidth(preferWidth);
			mainTP.setPrefWidth(preferWidth);
			portfolioPagination.setPrefWidth(preferWidth);
			transactionHistoryPagination.setPrefWidth(preferWidth);
		});
	}
	
	/**
	 * Initialize portfolio
	 */
	public void initPortfolio() {
		if (user != null) {
//			stocks = userStockManager.findStocks(user.getId());
			portfolioTransactions = transactionManager.findTransactions(user.getId(), CommonDefine.OWNED_STOCK);
//			portfolioPagination.setPageCount(stocks.size()/rowsPerPage + 1);
			portfolioPagination.setPageCount(portfolioTransactions.size()/rowsPerPage + 1);
//			table = createTable();
			portfolioTable = createPortfolioTable();
			portfolioPagination.setPageFactory(this::createPortfolioPage);
			// Sell stock when user click on button
			sellStockButton.setOnAction(event -> {
				// Only display alert when user select some stocks
				if (performingTransactions != null && performingTransactions.size() > 0) {
					Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.SELL_STOCK_SMS);
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						sellMutipleStocks(performingTransactions);
						// Refresh table view
						refreshTableView(performingTransactions);
						// Clear selected stock
						performingTransactions.clear();
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
//					makeNewStage(Screen.STOCK_DETAILS, "Stock Details", "../view/StockDetails.fxml");
					switchScreen(Screen.SELL_STOCK, "Sell Stock", "../view/SellStock.fxml");
				}
			});
		}
	}
	
	/**
	 * Initialize transaction history.
	 * Need to consider dynamic or lazy initialization
	 */
	public void initTransactionHistory() {
		if (user != null) {
			// Get both owned and sold transaction
			historyTransactions = transactionManager.findTransactions(user.getId(), CommonDefine.TRANSACTION_STOCK);
			transactionHistoryPagination.setPageCount(historyTransactions.size()/rowsPerPage + 1);
			historyTable = createHistoryTable();
			transactionHistoryPagination.setPageFactory(this::createTransactionPage);
		}
		
		exportButton.setOnAction(event -> {
			DirectoryChooser dc = new DirectoryChooser();
			File selectedDir = dc.showDialog(mainAP.getScene().getWindow());
			boolean exportSuccessful = ExportUtils.writeToExcel(historyTransactions, selectedDir.getAbsolutePath());
			if (exportSuccessful) {
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, "Exported successfully!");
				alert.showAndWait();
			} else {
				Alert alert = AlertFactory.generateAlert(AlertType.ERROR, "Exported failed! Try again.");
				alert.showAndWait();
			}
		});
	}
	
	/**
	 * <p>
	 * Sell owned stocks. Add performing transaction to database
	 * Add up earned money to balance and remove UserStock instance from database.
	 * </p>
	 * 
	 * @param performingTransactions List of selected stock in Portfolio
	 */
	private void sellMutipleStocks(List<TransactionWrapper> performingTransactions) {
		if (null == performingTransactions || performingTransactions.size() <= 0) {
			return;
		}
		
		// Sell stock one by one
		// Add up value to user account
		double curBalance = user.getAccount().getBalance();
		for (TransactionWrapper tran : performingTransactions) {
			// Get current price of stock
			try {
				curBalance += sellSingleStock(tran, curBalance);
			} catch (IOException e) {
				System.err.println("Transaction failed. Rollback.");
				e.printStackTrace();
			}
		}
		user.getAccount().setBalance(curBalance);
		userManager.update(user);
		// Show successful message
		Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.TRANSACTION_SUCCESSFUL_SMS);
		alert.showAndWait();
	}
	
	/**
	 * Sell stocks, update database
	 * @param tran
	 * @return Earned amount
	 * @throws IOException
	 */
	public double sellSingleStock(TransactionWrapper tran, double curBalance, int...soldAmount) throws IOException {
		double earnedAmount = 0;
		
		// Separate to sold transaction and remaining transaction
		// Store transaction when user didn't sell all stock
		Stock stock = tran.getStock();
		Stock soldStock = new Stock(stock); // Clone data
		
		yahoofinance.Stock yahooStock = yahoofinance.YahooFinance.get(stock.getStockCode());
		// Sell all owned stocks of this type
		if (soldAmount == null || soldAmount.length <= 0 || soldAmount[0] == stock.getAmount()) {
			earnedAmount = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue() * stock.getAmount();
		} else { // Sell only some stocks
			earnedAmount = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue() * soldAmount[0];
			soldStock.setAmount(soldAmount[0]);
			// Update remaining number of owned stock
//			stock.setAmount(stock.getAmount() - soldAmount[0]);
//			stockManager.update(stock);
			
			// Remaining transaction
			Stock remainingStock = new Stock(stock); // Clone data
			remainingStock.setAmount(stock.getAmount() - soldAmount[0]);
			Transaction remainingTransaction = new Transaction(user.getAccount(), new Date());
			// Copy previous transaction date
			remainingTransaction.setTransactionDate(tran.getTransaction().getTransactionDate());
			// Setup 1-to-1 mapping
			remainingStock.setTransaction(remainingTransaction);
			remainingTransaction.setStock(remainingStock);
			stockManager.add(remainingStock);
			userStockManager.add(user, remainingStock, CommonDefine.REMAINING_STOCK);
		}
		// Keep record, not query anymore. 
		UserStock us = userStockManager.findUserStock(user.getId(), stock.getId());
		// Not remove but update status of the stock: owned -> sold
		// Need to keep record to display in transaction history
		us.setStockType(CommonDefine.TRANSACTION_STOCK);
		userStockManager.update(us);
		
		
		// Create new transaction for a selling action
		Transaction soldTransaction = new Transaction(user.getAccount(), new Date());
		// Add payment and balance information to transaction
		soldTransaction.setPayment(earnedAmount);
//		t.setBalance(curBalance + earnedAmount);
		soldTransaction.setBalance(curBalance + earnedAmount);
		soldStock.setPrice(yahooStock.getQuote().getPrice());
		soldStock.setTransaction(soldTransaction);
		soldTransaction.setStock(soldStock);
		stockManager.add(soldStock);
		// Create new UserStock instance with [Sold] type in database
		userStockManager.add(user, soldStock, CommonDefine.TRANSACTION_STOCK);
		// Return earned amount
		return earnedAmount;
	}
	
	/**
	 * Refresh portfolio and transaction history view when user sold stocks
	 * 
	 * @param performingTransactions
	 */
	private void refreshTableView(List<TransactionWrapper> performingTransactions) {
		for (TransactionWrapper t : performingTransactions) {
			if (portfolioTable.getItems().contains(t)) { // Remove sold stock (transaction)
				portfolioTable.getItems().remove(t);
			}
		}
		// Refresh transaction history by pulling out data from database again and redraw table
		// TODO: It might take time, consider the way to update table without accessing database
		initTransactionHistory();
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
		            performingTransactions.add(item);
		        } else {
		        	performingTransactions.remove(item);
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

		TableColumn<TransactionWrapper, String> stockPriceCol = new TableColumn<>("Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPrice()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> amountCol = new TableColumn<>("Quantity");
		amountCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAmount()));
		amountCol.setPrefWidth(100);
		
		TableColumn<TransactionWrapper, String> paymentCol = new TableColumn<>("Payment");
		paymentCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTransactionPayment()));
		paymentCol.setPrefWidth(100);
		styleTableCell(paymentCol);

		TableColumn<TransactionWrapper, String> balanceCol = new TableColumn<>("Balance");
		balanceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBalance()));
		balanceCol.setPrefWidth(100);
		
		table.getColumns().addAll(transDateCol,  transTimeCol, stockCodeCol, stockNameCol, stockPriceCol, amountCol, paymentCol, balanceCol);
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
		TransactionWrapper tw = portfolioTable.getSelectionModel().getSelectedItem();
		switch(target) {
			case SELL_STOCK:
				SellStockController controller = loader.<SellStockController>getController();
				controller.setUser(user);
				controller.init(tw);
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				// Find selected stock in Java API
				// TODO: Think about better way to find the stock given stock code
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(tw.getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + tw.getStockCode());
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		TransactionWrapper tw = portfolioTable.getSelectionModel().getSelectedItem();
		switch(target) {
			case SELL_STOCK:
				SellStockController controller = loader.<SellStockController>getController();
				controller.setUser(user);
				controller.init(tw);
				break;
			default:
				return; // Move to undefined target. Should throw some exception?
		}
		Stage curStage = (Stage)mainAP.getScene().getWindow();
		curStage.setScene(new Scene(root));
		curStage.show();
	}
}
