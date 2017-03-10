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
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.UserStock;
import main.java.utility.AlertFactory;
import main.java.utility.Screen;
import main.java.utility.StageFactory;
import main.java.utility.Utility;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController extends BaseController implements Initializable {
	@FXML private AnchorPane homeAP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, String> priceCol;
	@FXML private TreeTableColumn<Stock, String> lastPriceCol;
	@FXML private TreeTableColumn<Stock, String> changeCol;
	@FXML private TreeTableColumn<Stock, String> percentChangeCol;
//	@FXML private JFXTreeTableColumn<Integer, String> stockBuyCol;
	
	@FXML private JFXTextField searchTF;
	@FXML private StackPane sp;
	@FXML private JFXSpinner spinner;
	
	private Stock stock; // The selected stock for alert
	
	private ObservableList<Stock> stocks;
	
	private final int REAL_TIME_UPDATE_STOCK_DURATION = 2;   // Download real-time stock data for each 2 minutes
	private final int ALERT_SETTINGS_CHECKING_DURATION = 30; // Check alert settings thresholds crossed for each 30 minutes
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// List of 30 stocks that will be displayed in Home page
		final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT",
													"TM", "KO", "HPQ", "FB", "F", "MSFT", 
													"BRK-A", "AMZN", "XOM", "JPM", "WFC", "GE",
													"BAC", "T", "BABA", "PG", "CVX", "V",
													"VZ", "HD", "DIS", "INTC", "ORCL", "HSBC"};
		// Background service to pull out real-time stock data
		RealTimeUpdateService service = new RealTimeUpdateService(stockSymbols);
		service.setPeriod(Duration.minutes(REAL_TIME_UPDATE_STOCK_DURATION));
		service.start();
		service.setOnSucceeded(event -> {
			System.out.println("Continue update...");
			stocks = service.getValue();
			 TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
			 stockTableView.setRoot(root);
			// Finish loading, hide spinner
			spinner.setVisible(false);
			System.out.println("Finish updating!");
		});
		
		// Background service to check alert settings status
//		userStockManager.findWithAlertSettingsOn();
		AlertSettingsCheckingService alertSettingsService = new AlertSettingsCheckingService(userStockManager);
		alertSettingsService.setPeriod(Duration.minutes(ALERT_SETTINGS_CHECKING_DURATION));
		alertSettingsService.start();
		alertSettingsService.setOnSucceeded(event -> {
			System.out.println("Checking alert settings...");
			ObservableList<UserStock> userStocks = alertSettingsService.getValue();
			showAlertStage(userStocks);
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
		        makeNewStage(Screen.ALERT_SETTINGS, "Alert Settings", "../view/AlertSettings.fxml");
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
					makeNewStage(Screen.STOCK_DETAILS, "Stock Details", "../view/StockDetails.fxml");
//					TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
//					System.out.println("Selected stock: " + item.getValue().getStockName());
				}
			}
			
		});
	}
	
	@Override
	protected void makeNewStage(Screen target, String stageTitle, String url) {
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
		makeNewStage(Screen.SETTINGS, "Settings", "../view/Settings.fxml");
	}
	
	@FXML private void openPorfolio(ActionEvent event) {
		makeNewStage(Screen.PORFOLIO, "My Porfolio", "../view/Portfolio.fxml");
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
		changeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangeString()));
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangePercentString()));
		//setPriceFormatColumn(percentChangeCol);
	}
	
	/**
	 * Create a list of stocks whole values crossed threshold
	 * @param stocks
	 */
	private void showAlertStage(ObservableList<UserStock> userStocks) {
		if (userStocks != null && userStocks.size() > 0) {
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
					 return Utility.getMultipleStockData(stockSymbols);
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
		
		public AlertSettingsCheckingService(UserStockManager<UserStock> usManager) {
			this.usManager = usManager;
		}
		
		@Override
		protected Task<ObservableList<UserStock>> createTask() {
			return new Task<ObservableList<UserStock>>() {
				@Override
				protected ObservableList<UserStock> call() throws IOException {
					List<UserStock> userStocks = usManager.findWithAlertSettingsOn();
					if (userStocks == null) {
						return null;
					}
//					BigDecimal defaultThreshold = new BigDecimal(-1);
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
						if (us.getValueThreshold().compareTo(curPrice) < 0) {
							thresholdCrossed = true;
						}
						
						// Check combined value threshold crossed
						BigDecimal combinedValue = curPrice.multiply(BigDecimal.valueOf(stock.getAmount()));
						if (us.getCombinedValueThreshold().compareTo(combinedValue) < 0) {
							thresholdCrossed = true;
						}
						
						// Set current value for UserStock
						us.setCurrentValueThreshold(curPrice);
						us.setCurrentCombinedValueThreshold(combinedValue);
						
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
}
