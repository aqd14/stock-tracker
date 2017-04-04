/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.User;
import main.java.model.UserStock;
import main.java.notification.EmailNotification;
import main.java.notification.PhoneNotification;
import main.java.utility.AlertFactory;
import main.java.utility.Screen;
import main.java.utility.StageFactory;
import main.java.utility.Utils;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController extends BaseController implements Initializable, Observer, IController {
	@FXML private StackPane homeSP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, String> priceCol;
	@FXML private TreeTableColumn<Stock, String> lastPriceCol;
	@FXML private TreeTableColumn<Stock, String> priceChangeCol;
	@FXML private TreeTableColumn<Stock, String> percentChangeCol;
//	@FXML private JFXTreeTableColumn<Integer, String> stockBuyCol;
	
	@FXML private JFXTextField searchTF;
	@FXML private StackPane sp;
	@FXML private JFXSpinner spinner;
	@FXML private ImageView addStock;
	
	private Stock stock; // The selected stock for alert
	private ObservableList<Stock> stocks;
	
	// List of 30 stocks that will be displayed in Home page
	final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT",
												"TM", "KO", "HPQ", "FB", "F", "MSFT", 
												"BRK-A", "AMZN", "XOM", "JPM", "WFC", "GE",
												"BAC", "T", "BABA", "PG", "CVX", "V",
												"VZ", "HD", "DIS", "INTC", "ORCL", "HSBC"};
	
	RealTimeUpdateService stockUpdateService;
	AlertSettingsCheckingService alertSettingsService;
	
	PhoneNotification phoneNotif = new PhoneNotification();
	EmailNotification emailNotif = new EmailNotification();
	
	/**
	 * Update schedule service period whenever there is update from user's settings
	 */
	@Override public void update() {
		if (stockUpdateService != null)
			stockUpdateService.setPeriod(Duration.minutes(user.getStockUpdateTime()));
		if (alertSettingsService != null)
			alertSettingsService.setPeriod(Duration.minutes(user.getAlertTime()));
	}
	
	@Override 
	public void setUser(User user) {
		// TODO Auto-generated method stub
		super.setUser(user);
		startScheduleService();
	}
	
	/**
	 * Start background services including:
	 * <li> Downloading real-time stock data </li>
	 * <li> Periodically check alert threshold reached or not </li>
	 */
	private void startScheduleService() {
		// Background service to pull out real-time stock data
		stockUpdateService = new RealTimeUpdateService(stockSymbols);
		stockUpdateService.setPeriod(Duration.minutes(user.getStockUpdateTime()));
		stockUpdateService.start();
		stockUpdateService.setOnSucceeded(event -> {
			System.out.println("Continue update...");
			stocks = stockUpdateService.getValue();
			 TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
			 stockTableView.setRoot(root);
			// Finish loading, hide spinner
			spinner.setVisible(false);
			System.out.println("Finish updating!");
		});
		
		// Background service to check alert settings status
		alertSettingsService = new AlertSettingsCheckingService(user.getId(), userStockManager);
		alertSettingsService.setPeriod(Duration.minutes(user.getAlertTime()));
		alertSettingsService.start();
		alertSettingsService.setOnSucceeded(event -> {
			System.out.println("Checking alert settings...");
			ObservableList<UserStock> userStocks = alertSettingsService.getValue();
			notifyAlertToUser(userStocks);
		});
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addStock.setOnMouseClicked(event -> {
			makeNewStage(Screen.ADD_STOCK, "Add new stock", "../view/AddStock.fxml");
		});
		// Initialize GUI
		stocks = FXCollections.observableArrayList();
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		stockTableView.setShowRoot(false);
		stockTableView.setEditable(true);
		// Set Cell Value Factory
		setCellValueStockCode();
		setCellValueCompanyName();
		setCellValueStockPrice();
		setCellFactoryLastPrice();
		setCellFactoryPriceChange();
		setCellFactoryPercentageChange();
		// Initialize context menu when user click right mouse on a row
		stockTableView.setRowFactory(ttv -> {
		    ContextMenu contextMenu = new ContextMenu();
		    MenuItem alertSettingsItem = new MenuItem("Alert Settings");
		    // ...
		    MenuItem removeItem = new MenuItem("Remove Stock");
		    // ...
		    contextMenu.getItems().addAll(alertSettingsItem, removeItem);
		    TreeTableRow<Stock> row = new TreeTableRow<Stock>() {
		        @Override
		        public void updateItem(Stock item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		                setContextMenu(null);
		            } else {
		                // configure context menu with appropriate menu items, 
		                // depending on value of item
		                setContextMenu(contextMenu);
		            }
		        }
		    };
		    alertSettingsItem.setOnAction(evt -> {
		        stock = row.getItem();
		        makeNewStage(Screen.ALERT_SETTINGS, CommonDefine.ALERT_SETTINGS_TITLE, "../view/AlertSettings.fxml");
		    });
		    // event handlers for other menu items...
		    removeItem.setOnAction(evt -> {
		    	// Display dialog warning users
		    	// when they try to remove an owned stock
		    	String stockCode = row.getItem().getStockCode();
		    	if (userStockManager.hasStock(user.getId(), stockCode)) {
		    		// Display alert to notify user before removing an owned stock
		    		Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.REMOVE_STOCK_SMS);
		    		Optional<ButtonType> result = alert.showAndWait();
		    		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
		    			// User cancels removing
		    			return;
		    		}
		    	} else {
		    		// Do nothing, just remove selected stock
		    	}
		    	// Remove selected stock from table view
		    	TreeItem<Stock> treeItem = row.getTreeItem();
		    	treeItem.getParent().getChildren().remove(treeItem);
		    	stockTableView.getSelectionModel().clearSelection();
		    });
		    return row ;
		});
		
		// Add listener for search field
		searchTF.textProperty().addListener((o,oldVal,newVal)->{
			stockTableView.setPredicate(stock -> filterCriteria(stock, newVal));
		});
		
		// Add listener when user select a row
		stockTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
//				System.out.println("Current number of rows: " + stockTableView.getCurrentItemsCount());
				if (mouseEvent.getClickCount() == 2 && stockTableView.getCurrentItemsCount() > 0) { // Double click
					makeNewStage(Screen.STOCK_DETAILS, CommonDefine.STOCK_DETAILS_TITLE, "../view/StockDetails.fxml");
//					TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
//					System.out.println("Selected stock: " + item.getValue().getStockName());
				}
			}
			
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public void makeNewStage(Screen target, String stageTitle, String url) {
		Stage newStage = StageFactory.generateStage(stageTitle);
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
			case ALERT_SETTINGS:
				AlertSettingsController alertSettingsController = loader.<AlertSettingsController>getController();
				alertSettingsController.setUser(user);
				alertSettingsController.initAlertSettings(stock);
				break;
			case PORFOLIO:
				PortfolioController portfolioController = loader.<PortfolioController>getController();
				portfolioController.setUser(user);
				portfolioController.initPortfolio();
				portfolioController.initTransactionHistory();
				break;
			case SETTINGS:
				// Set current user
				SettingsController settingsController = loader.<SettingsController>getController();
				settingsController.setUser(user);
				settingsController.initUserInfo();
				// Subscribe to get updates from user's settings
				settingsController.register(this);
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				// Find selected stock in Java API
				TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
				// TODO: Think about better way to find the stock given stock code
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(item.getValue().getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + item.getValue().getStockCode());
					e.printStackTrace();
					return;
				}
				stockController.setStock(yahooStock);
				stockController.updateStockData();
				// Stop updating stock data in Stock Detail view whenever user closes view
				newStage.setOnCloseRequest(eventHandler -> {
					stockController.setStock(null);
				});
				break;
			case ADD_STOCK:
				break;
			default:
				return;
		}
		newStage.setScene(new Scene(root));
		newStage.show();
	}
	
	/**
	 * Open new screen when user select [Settings] on Menu bar
	 * @param event Capture the action user performed. 
	 */
	@FXML private void openAccountSettings(ActionEvent event) {
		makeNewStage(Screen.SETTINGS, CommonDefine.USER_SETTINGS_TITLE, "../view/Settings.fxml");
	}
	
	@FXML private void openPorfolio(ActionEvent event) {
		makeNewStage(Screen.PORFOLIO, CommonDefine.PORTFOLIO_TITLE, "../view/Portfolio.fxml");
	}
	
	@FXML private void exit(ActionEvent event) {
		Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, "Do you really want to exit?");
		Optional<ButtonType> selection = alert.showAndWait();
		if (selection.isPresent() && selection.get().equals(ButtonType.OK)) {
			switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, "../../../main/java/view/Login.fxml");
		} else {
			// Stay in Home page
		}
	}

	/**
	 * <p>
	 * Filter stock based on stock code or company name. It searches if stock code or company name 
	 * contains search query.
	 * </p>
	 * @param stock
	 * @param value
	 * @return <code>True</code> if matched, otherwise return <code>False</code> 
	 */
	private boolean filterCriteria(TreeItem<Stock> stock, String value) {
		return stock.getValue().getStockCode().toLowerCase().contains(value.toLowerCase()) ||
				stock.getValue().getStockName().toLowerCase().contains(value.toLowerCase());
	}
	
	private void setCellValueStockCode() {
		stockCodeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockCode()));
	}
	
	private void setCellValueCompanyName() {
		companyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockName()));
	}
	
	private void setCellValueStockPrice() {
		priceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceString()));
	}
	
	private void setCellFactoryLastPrice() {
		lastPriceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPreviousPriceString()));
	}
	
	private void setCellFactoryPriceChange() {
		priceChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangeString()));
		// Update cell color based on price change
		priceChangeCol.setCellFactory(param -> new TreeTableCell<Stock, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				if (!empty) {
					super.updateItem(item, empty);
					item = item.replaceAll(",", "");
					StringBuilder bd = new StringBuilder();
					double priceChange = Double.valueOf(item);
					// Price went down
					if (priceChange < 0) {
						setTextFill(Color.RED);
						bd.append("- ").append(item.replace("-", ""));
					} else if (priceChange > 0) { // Price went up
						setTextFill(Color.GREEN);
						bd.append("+ ").append(item);
					} else {
						// Price stays the same.
						// Reset to black
						setTextFill(Color.BLACK);
						bd.append(item);
					}
					setText(bd.toString());
				}
			}
		});
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangePercentString()));
		//setPriceFormatColumn(percentChangeCol);
	}
	
	/**
	 * Notify user about some updates in alert settings. 
	 * Some threshold might have been crossed.
	 * 
	 * @param userStocks
	 */
	private void notifyAlertToUser(ObservableList<UserStock> userStocks) {
		if (userStocks != null && userStocks.size() > 0) {
			showAlertStage(userStocks);
			sendMessageToUser(userStocks);
		}
	}
	
	/**
	 * Send a message that contains a list of Stocks whose some thresholds have been crossed.
	 * 
	 * @param userStocks
	 */
	private void sendMessageToUser(ObservableList<UserStock> userStocks) {
		StringBuilder builder = new StringBuilder("Here is the list of stocks which have thresholds crossed: ");
		for (int i = 0; i < userStocks.size(); i ++) {
			UserStock us = userStocks.get(i);
			builder.append(us.getStock().getStockCode());
			// Format message sent to user's phone
			if (i != userStocks.size() - 1) {
				builder.append(", ");
			}
		}
//		CommunicationUtil.sendMessage(builder.toString(), user.getPhoneNumber());
		String sms = builder.toString();
		phoneNotif.notify(sms, user.getPhoneNumber());
		emailNotif.notify(sms, user.getEmail());
	}
	
	/**
	 * Create a list of stocks whole values crossed threshold
	 * @param stocks
	 */
	private void showAlertStage(ObservableList<UserStock> userStocks) {
		TableView<UserStock> alertTable = createAlertTable(userStocks);
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(alertTable);
		Stage stage = StageFactory.generateStage("Stock Alert");
		stage.setScene(new Scene(vbox));
		stage.show();
		stage.setOnCloseRequest(event -> { // Clean up database when user close stage
			clean(userStocks);
		});
	}
	
	@SuppressWarnings("unchecked")
	private TableView<UserStock> createAlertTable(ObservableList<UserStock> us) {
		// Only display what threshold have been crossed
//		List <TableColumn<UserStock, String>> columns = new ArrayList<>();
		TableView<UserStock> table = new TableView<>();
		table.setEditable(true);
		
		TableColumn<UserStock, String> stockCodeCol = new TableColumn<>("Stock Code");
		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getStockCode()));
		stockCodeCol.setPrefWidth(80);

//		TableColumn<UserStock, String> stockNameCol = new TableColumn<>("Company");
//		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getStockName()));
//		stockNameCol.setPrefWidth(250);

		TableColumn<UserStock, String> stockPriceCol = new TableColumn<>("Bought Price");
		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getPriceString()));
		stockPriceCol.setPrefWidth(100);
		
		TableColumn<UserStock, String> valueThresholdCol = new TableColumn<>("Value Threshold");
		valueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValueThresholdString()));
		valueThresholdCol.setPrefWidth(120);
		
		TableColumn<UserStock, String> combinedValueThresholdCol = new TableColumn<>("Combined Value Threshold");
		combinedValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCombinedValueThresholdString()));
		combinedValueThresholdCol.setPrefWidth(200);
		
		TableColumn<UserStock, String> netProfitThresholdCol = new TableColumn<>("Net Profit Threshold");
		netProfitThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getNetProfitThresholdString()));
		netProfitThresholdCol.setPrefWidth(150);
		
		TableColumn<UserStock, String> currentValueThresholdCol = new TableColumn<>("Current Value Threshold");
		currentValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentValueThresholdString()));
		currentValueThresholdCol.setPrefWidth(180);
		
		TableColumn<UserStock, String> currentCombinedValueThresholdCol = new TableColumn<>("Current Combined Value Threshold");
		currentCombinedValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentCombinedValueThresholdString()));
		currentCombinedValueThresholdCol.setPrefWidth(250);
		
		TableColumn<UserStock, String> currentNetProfitThresholdCol = new TableColumn<>("Current Net Profit Threshold");
		currentNetProfitThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentNetProfitThresholdString()));
		currentNetProfitThresholdCol.setPrefWidth(250);
		
//		TableColumn<TransactionWrapper, Boolean> sellStockCol = new TableColumn<>();
//		sellStockCol.setGraphic(new CheckBox());
//		
//		sellStockCol.setCellValueFactory(new PropertyValueFactory<TransactionWrapper, Boolean>("selected"));
//		// Add event handler when users select a check box on table
//		sellStockCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
//		    @Override
//		    public ObservableValue<Boolean> call(Integer index) {
//		        TransactionWrapper item = table.getItems().get(index);
//		        // Add/Remove item from the selected list
//		        if (true == item.getSelected()) {
//		            selectedStock.add(item.getStock());
//		        } else {
//		        	selectedStock.remove(item.getStock());
//		        }
//		        return item.selectedProperty();
//		    }
//		}));
//		sellStockCol.setEditable(true);
		table.setItems(us);
		table.getColumns().addAll(stockCodeCol, stockPriceCol, valueThresholdCol, 
								currentValueThresholdCol, combinedValueThresholdCol, 
								currentCombinedValueThresholdCol, netProfitThresholdCol, 
								currentNetProfitThresholdCol);
		return table;
	}
	
	/**
	 * Clean up database after showing user alert.
	 * Set all threshold values that have been crossed to -1 (Reset) 
	 * 
	 * @param userStocks
	 */
	private void clean(ObservableList<UserStock> userStocks) {
		BigDecimal defaultThreshold = new BigDecimal(-1);
		for (UserStock us : userStocks) {
			BigDecimal valueThreshold = us.getValueThreshold();
			BigDecimal curValueThreshold = us.getCurrentValueThreshold();
			BigDecimal combinedThreshold = us.getCombinedValueThreshold();
			BigDecimal curCombinedThreshold = us.getCurrentCombinedValueThreshold();
			BigDecimal netProfitThreshold = us.getNetProfitThreshold();
			BigDecimal curNetProfitThreshold = us.getCurrentNetProfitThreshold();
			// Whenever a threshold reached, reset to default value
			if (valueThreshold != null && curValueThreshold != null && valueThreshold.compareTo(curValueThreshold) < 0) {
				us.setValueThreshold(defaultThreshold);
			}
			if (combinedThreshold != null && curCombinedThreshold != null && combinedThreshold.compareTo(curCombinedThreshold) < 0) {
				us.setCombinedValueThreshold(defaultThreshold);
			}
			if (netProfitThreshold != null && curNetProfitThreshold != null && netProfitThreshold.compareTo(curNetProfitThreshold) < 0) {
				us.setNetProfitThreshold(defaultThreshold);
			}
			userStockManager.update(us);
		}
	}
	
	/**
	 * <p>
	 * Scheduled service to automatically download real-time stock information.
	 * Default downloading time interval is 2 minutes. 
	 * </p>
	 * @author doquocanh-macbook
	 *
	 */
	private static class RealTimeUpdateService extends ScheduledService<ObservableList<Stock>> {
		String[] stockSymbols;
		
		public RealTimeUpdateService(String[] stockSymbols) {
			this.stockSymbols = stockSymbols;
		}
		
		@Override
		protected Task<ObservableList<Stock>> createTask() {
			return new Task<ObservableList<Stock>>() {
				@Override
				protected ObservableList<Stock> call() throws IOException {
					 return Utils.getMultipleStockData(stockSymbols);
				}
			};
		}
	}
	
	/**
	 * <p>
	 * Scheduled service to check if any threshold of Alert Settings crossed
	 * Default checking time interval is 30 minutes. 
	 * </p>
	 * @author doquocanh-macbook
	 *
	 */
	private static class AlertSettingsCheckingService extends ScheduledService<ObservableList<UserStock>> {
		
		private UserStockManager<UserStock> usManager;
		private Integer userID;
		
		public AlertSettingsCheckingService(Integer userID, UserStockManager<UserStock> usManager) {
			this.userID = userID;
			this.usManager = usManager;
		}
		
		/**
		 * Search database for list of stocks then calculate combined value threshold.
		 * 
		 * @param stock Current stock that needs to calculate combined threshold
		 * @return
		 */
		private BigDecimal calculatePreviousCombinedValueThreshold(yahoofinance.Stock stock) {
			List<Stock> stocks = usManager.findStocks(userID, stock.getSymbol());
			if (stocks != null && stocks.size() > 0) {
				BigDecimal total = new BigDecimal(0);
				for (Stock s : stocks) {
					total = total.add(s.getPrice().multiply(BigDecimal.valueOf(s.getAmount())));
				}
				return total;
			}
			return new BigDecimal(-1);
		}
		
		/**
		 * Calculate combined value of stock with current price
		 * 
		 * @param stock
		 * @return
		 */
		private BigDecimal calculateCurrentCombinedValueThreshold(yahoofinance.Stock stock) {
			List<Stock> stocks = usManager.findStocks(userID, stock.getSymbol());
			BigDecimal total = new BigDecimal(-1);
			if (stocks != null && stocks.size() > 0) {
				int totalAmount = 0;
				for (Stock s : stocks) {
					totalAmount += s.getAmount();
				}
				total = stock.getQuote().getPrice().multiply(BigDecimal.valueOf(totalAmount));
			}
			return total;
		}
		

		/**
		 * <p>
		 * Check if threshold is crossed or not. There are two possibilities:
		 * 
		 * <li>
		 * 1. At the time when user set threshold, the stock price is higher than threshold value,
		 * which means user was looking for the alert when stock price went down
		 * </li>
		 * 
		 * <li>
		 * 2. Conversely, if the stock price was lower than threshold value when user setting, 
		 * he is looking for the alert when price raising
		 * </li>
		 * </p>
		 *
		 * 
		 * @param threshold			The threshold value
		 * @param previousPrice		The price at the moment user settings
		 * @param curPrice			Current price of stock
		 * @return	<code>True</code> if the price went below or above threshold. Otherwise, returns <code>False</code> 
		 */
		private boolean isThresholdCrossed(BigDecimal threshold, BigDecimal previousPrice, BigDecimal curPrice) {
			// At the time user set threshold, the value of threshold is lesser than the stock price
			// So, we check if the current price went down below threshold or not
			if (threshold.compareTo(previousPrice) < 0) {
				if (threshold.compareTo(curPrice) > 0) {
					return true;
				}
			} else {
				if (threshold.compareTo(curPrice) < 0) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * <p>
		 * Check if threshold is crossed or not. There are two possibilities:
		 * 
		 * <li>
		 * 1. At the time when user set threshold, the stock price is higher than threshold value,
		 * which means user was looking for the alert when stock price went down
		 * </li>
		 * 
		 * <li>
		 * 2. Conversely, if the stock price was lower than threshold value when user setting, 
		 * he is looking for the alert when price raising
		 * </li>
		 * </p>
		 *
		 * 
		 * @param threshold			The threshold value
		 * @param previousPrice		The price at the moment user settings
		 * @param curPrice			Current price of stock
		 * @return	<code>True</code> if the price went below or above threshold. Otherwise, returns <code>False</code> 
		 */
		private boolean isNetThresholdCrossed(BigDecimal threshold, BigDecimal previousPrice, BigDecimal curPrice) {
			// TODO: Need to find a way to check if user wants to check net profit or net loss
			BigDecimal difference = curPrice.subtract(previousPrice);
			if (difference.compareTo(BigDecimal.ZERO) > 0) { // User is earning money
				if (threshold.compareTo(difference) < 0) {	 // Therefore, user probably wants to check net profit
					return true;
				}
			} else { // User is losing money, he might want to check net loss
				BigDecimal minusOne = new BigDecimal(-1);
				// Reverse value of difference than compare with threshold value
				if (threshold.compareTo(difference.multiply(minusOne)) < 0) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		protected Task<ObservableList<UserStock>> createTask() {
			return new Task<ObservableList<UserStock>>() {
				@Override
				protected ObservableList<UserStock> call() throws IOException {
					List<UserStock> userStocks = usManager.findWithAlertSettingsOn(userID);
					if (userStocks == null) {
						return null;
					}
					BigDecimal defaultThreshold = new BigDecimal(-1);
					// Get current quotes of list of stocks to check thresholds reached or not
					// If reached, initialize current values for those threshold. If not, 
					// remove from UserStock list
					List<UserStock> removeList = new ArrayList<UserStock>();
					for (UserStock us : userStocks) {
						boolean thresholdCrossed = false;
						Stock stock = us.getStock();
						yahoofinance.Stock yahooStock = YahooFinance.get(stock.getStockCode());
						BigDecimal curPrice = yahooStock.getQuote().getPrice();
						// Check value threshold crossed
						if (us.getValueThreshold().compareTo(defaultThreshold) > 0) {
							thresholdCrossed = thresholdCrossed || isThresholdCrossed(us.getValueThreshold(), us.getStock().getPrice(), curPrice);
						}
						
						// Check combined value threshold crossed
						BigDecimal previousCombinedValue = calculatePreviousCombinedValueThreshold(yahooStock);
						BigDecimal curCombinedValue = calculateCurrentCombinedValueThreshold(yahooStock);
						if (us.getCombinedValueThreshold().compareTo(defaultThreshold) > 0) {
							thresholdCrossed = thresholdCrossed || isThresholdCrossed(us.getCombinedValueThreshold(), previousCombinedValue, curCombinedValue);
						}
						
						// Check net gain/net loss
						if (us.getNetProfitThreshold().compareTo(defaultThreshold) > 0) {
							thresholdCrossed = thresholdCrossed || isNetThresholdCrossed(us.getNetProfitThreshold(), previousCombinedValue, curCombinedValue);
						}
						
						// Set threshold values for display purpose later
						us.setCurrentValueThreshold(curPrice);
//						us.setCurrentCombinedValueThreshold(curr);
						
						if (thresholdCrossed) { // Reset threshold in database
//							usManager.update(us);
						} else {
							removeList.add(us); // No threshold crossed, remove from the list to avoid displaying
						}
						// TODO: Confirm about net gains/losses
					}
					userStocks.removeAll(removeList);
					return FXCollections.observableArrayList(userStocks);
				}
			};
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void switchScreen(Screen target, String title, String url) {
        Parent root = null;
		try {
			switch (target) {
				case LOGIN:
					root = new FXMLLoader(getClass().getResource(url)).load();
					break;
				default:
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	  	Stage curStage = (Stage)homeSP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
	}
}
