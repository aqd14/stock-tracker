/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.User;
import main.java.model.UserStock;
import main.java.notification.EmailNotification;
import main.java.notification.PhoneNotification;
import main.java.service.AlertSettingsCheckingService;
import main.java.service.RealTimeUpdateService;
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
	
	private ArrayList<String> stockSymbolsArrayList;
	private String[] stockSymbolsArray;
	
	private boolean isFirstLogin = false;
	
	RealTimeUpdateService stockUpdateService;
	AlertSettingsCheckingService alertSettingsService;
	
	PhoneNotification phoneNotif = new PhoneNotification();
	EmailNotification emailNotif = new EmailNotification();
	
	public ArrayList<String> getStocks() {
		return this.stockSymbolsArrayList;
	}
	
	/**
	 * <p>
	 * Initialize list of stock that will be displayed in [Home] page
	 * when user login into system.
	 * 
	 * Get the list of interested stocks in database. If the list is empty, using default stocks
	 * to give user some suggestion. User can remove these stocks or add others later
	 * </p>
	 */
	private void initializeStockList() {
		// List of default 30 stocks that will be displayed in [Home] page
		// if user doesn't add any stock into view list
//		ArrayList<String> stockSymbolsArrayList;
		List<UserStock> interestedStockList = userStockManager.findInterestedStockList(user.getId());
		// Extract list of stock user is interested in
		if (interestedStockList == null || interestedStockList.size() <= 0) {
			isFirstLogin = true;
			stockSymbolsArrayList = new ArrayList<>(Arrays.asList(CommonDefine.DEFAULT_INTERESTED_STOCKS));
		} else {
			isFirstLogin = false;
			stockSymbolsArrayList = new ArrayList<String>();
			for(UserStock us : interestedStockList) {
				stockSymbolsArrayList.add(us.getStock().getStockCode());
			}
		}
	}
	
	/**
	 * Keep track the list of stock symbols user are watching. 
	 * Update table view when user added a new stock
	 * 
	 * @param stockSymbol
	 */
	public void populateData(String stockSymbol) {
		synchronizeStockList();
		String[] list = {stockSymbol};
		try {
			Stock newStock = (Stock) Utils.getMultipleStockData(list).get(0);
			// Add new stock to interested list
			stocks.add(newStock);
			// Add new UserStock instance to db
			stockManager.add(newStock);
			userStockManager.add(user, newStock, CommonDefine.INTERESTED_STOCK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		System.out.println("Finish adding!");
	}
	
	public void synchronizeStockList() {
		stockSymbolsArray = stockSymbolsArrayList.toArray(new String[0]);
	}
	
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
		this.user = user;
		// Initialize stock list
		initializeStockList();
		// Convert ArrayList to String array to pull out stock data
		// YahooFinance APIs work only with String array
		synchronizeStockList();
//		stockSymbols = stockSymbolsArrayList.toArray(new String[0]);
		// Start pulling out stock data
		startScheduleService();
	}
	
	/**
	 * Start background services including:
	 * <li> Downloading real-time stock data </li>
	 * <li> Periodically check alert threshold reached or not </li>
	 */
	private void startScheduleService() {
		startStockUpdateService();
		startAlertSettingsService();
	}
	
	/**
	 * Only add list of interested in database when this is 
	 * the first time user login into system.
	 */
	private void startStockUpdateService() {
		// Background service to pull out real-time stock data
		stockUpdateService = new RealTimeUpdateService(stockSymbolsArray);
		stockUpdateService.setPeriod(Duration.minutes(user.getStockUpdateTime()));
		stockUpdateService.start();
		stockUpdateService.setOnSucceeded(event -> {
			System.out.println("Continue update...");
			spinner.setVisible(true);
			stocks = stockUpdateService.getValue();
			TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
			stockTableView.setRoot(root);
			// Finish loading, hide spinner
			spinner.setVisible(false);
			// If this is the first login, add list of default stock to database
			// Else, don't need to do anything
			if (isFirstLogin) {
				System.out.println("This is the first time login!");
				for (Stock s : stocks) {
					// Add new interested stock and new user-stock instances to database
					stockManager.add(s);
					userStockManager.add(user, s, CommonDefine.INTERESTED_STOCK);
				}
			} else {
				System.out.println("This is not the first time login!");
			}
			System.out.println("Finish updating!");
		});
	}
	
	private void startAlertSettingsService() {
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
//		    	if (userStockManager.hasStock(user.getId(), stockCode)) {
		    		// Display alert to notify user before removing an owned stock
	    		Alert alert = AlertFactory.generateAlert(AlertType.CONFIRMATION, CommonDefine.REMOVE_STOCK_SMS);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
	    			// User cancels removing
	    			return;
	    		}
//		    	} else {
//		    		// Do nothing, just remove selected stock
//		    	}
	    		// Remove stock symbol from interested list
	    		// to avoid real-time update it
	    		stockSymbolsArrayList.remove(stockCode);
	    		synchronizeStockList();
	    		// Remove from database
	    		// Remove stock and associate UserStock instance
	    		UserStock us = userStockManager.findInterestedStock(user.getId(), stockCode);
	    		Stock removedStock = us.getStock();
	    		userStockManager.remove(us);
	    		stockManager.remove(removedStock);
//	    		userStockManager.remove();
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
				AddStockController addStockContrpller = loader.<AddStockController>getController();
				addStockContrpller.setHomeController(this);
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
			// Stop schedule services
			stockUpdateService.cancel();
			alertSettingsService.cancel();
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
				if (empty) {
					setText(null);
					setGraphic(null);
				}
				else {
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
//			showAlertStage(userStocks);
			sendMessageToUser(userStocks);
		}
	}
	
	/**
	 * Send a message that contains a list of Stocks whose some thresholds have been crossed.
	 * 
	 * @param userStocks
	 */
	private void sendMessageToUser(ObservableList<UserStock> userStocks) {
		// Make a list of non-duplicate alert stocks to send to user
		HashSet<String> stockSet = new HashSet<String>();
		for (UserStock us : userStocks) {
			stockSet.add(us.getStock().getStockCode());
		}
		
		StringBuilder builder = new StringBuilder("Here is the list of stocks which have thresholds crossed: ");
		Iterator<String> it = stockSet.iterator();
		int size = stockSet.size();
		int i = 1;
		while (it.hasNext()) {
			builder.append(it.next());
			// Format message sent to user's phone
			if (i != size) {
				builder.append(", ");
			}
			i ++;
		}
//		CommunicationUtil.sendMessage(builder.toString(), user.getPhoneNumber());
		String sms = builder.toString();
		phoneNotif.notify(sms, user.getPhoneNumber());
		emailNotif.notify(sms, user.getEmail());
	}
	
//	/**
//	 * Create a list of stocks whole values crossed threshold
//	 * @param stocks
//	 */
//	@SuppressWarnings("unused") // Don't need to show alert stage at this time. Only sending email and message is enough
//	private void showAlertStage(ObservableList<UserStock> userStocks) {
//		TableView<UserStock> alertTable = createAlertTable(userStocks);
//        final VBox vbox = new VBox();
//        vbox.setSpacing(5);
//        vbox.setPadding(new Insets(10, 0, 0, 10));
//        vbox.getChildren().add(alertTable);
//		Stage stage = StageFactory.generateStage("Stock Alert");
//		stage.setScene(new Scene(vbox));
//		stage.show();
////		stage.setOnCloseRequest(event -> { // Clean up database when user close stage
////			clean(userStocks);
////		});
//		// Clean database when ever a threshold crossed after alerting user
//		clean(userStocks);
//	}
//	
//	@SuppressWarnings("unchecked")
//	private TableView<UserStock> createAlertTable(ObservableList<UserStock> us) {
//		// Only display what threshold have been crossed
////		List <TableColumn<UserStock, String>> columns = new ArrayList<>();
//		TableView<UserStock> table = new TableView<>();
//		table.setEditable(true);
//		
//		TableColumn<UserStock, String> stockCodeCol = new TableColumn<>("Stock Code");
//		stockCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getStockCode()));
//		stockCodeCol.setPrefWidth(80);
//
////		TableColumn<UserStock, String> stockNameCol = new TableColumn<>("Company");
////		stockNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getStockName()));
////		stockNameCol.setPrefWidth(250);
//
//		TableColumn<UserStock, String> stockPriceCol = new TableColumn<>("Bought Price");
//		stockPriceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getPriceString()));
//		stockPriceCol.setPrefWidth(100);
//		
//		TableColumn<UserStock, String> valueThresholdCol = new TableColumn<>("Value Threshold");
//		valueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValueThresholdString()));
//		valueThresholdCol.setPrefWidth(120);
//		
//		TableColumn<UserStock, String> combinedValueThresholdCol = new TableColumn<>("Combined Value Threshold");
//		combinedValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCombinedValueThresholdString()));
//		combinedValueThresholdCol.setPrefWidth(200);
//		
//		TableColumn<UserStock, String> netProfitThresholdCol = new TableColumn<>("Net Profit Threshold");
//		netProfitThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getNetProfitThresholdString()));
//		netProfitThresholdCol.setPrefWidth(150);
//		
//		TableColumn<UserStock, String> currentValueThresholdCol = new TableColumn<>("Current Value Threshold");
//		currentValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentValueThresholdString()));
//		currentValueThresholdCol.setPrefWidth(180);
//		
//		TableColumn<UserStock, String> currentCombinedValueThresholdCol = new TableColumn<>("Current Combined Value Threshold");
//		currentCombinedValueThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentCombinedValueThresholdString()));
//		currentCombinedValueThresholdCol.setPrefWidth(250);
//		
//		TableColumn<UserStock, String> currentNetProfitThresholdCol = new TableColumn<>("Current Net Profit Threshold");
//		currentNetProfitThresholdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentNetProfitThresholdString()));
//		currentNetProfitThresholdCol.setPrefWidth(250);
//		
////		TableColumn<TransactionWrapper, Boolean> sellStockCol = new TableColumn<>();
////		sellStockCol.setGraphic(new CheckBox());
////		
////		sellStockCol.setCellValueFactory(new PropertyValueFactory<TransactionWrapper, Boolean>("selected"));
////		// Add event handler when users select a check box on table
////		sellStockCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
////		    @Override
////		    public ObservableValue<Boolean> call(Integer index) {
////		        TransactionWrapper item = table.getItems().get(index);
////		        // Add/Remove item from the selected list
////		        if (true == item.getSelected()) {
////		            selectedStock.add(item.getStock());
////		        } else {
////		        	selectedStock.remove(item.getStock());
////		        }
////		        return item.selectedProperty();
////		    }
////		}));
////		sellStockCol.setEditable(true);
//		table.setItems(us);
//		table.getColumns().addAll(stockCodeCol, stockPriceCol, valueThresholdCol, 
//								currentValueThresholdCol, combinedValueThresholdCol, 
//								currentCombinedValueThresholdCol, netProfitThresholdCol, 
//								currentNetProfitThresholdCol);
//		return table;
//	}
//	
//	/**
//	 * Clean up database after showing user alert.
//	 * Set all threshold values that have been crossed to -1 (Reset) 
//	 * 
//	 * @param userStocks
//	 */
//	private void clean(ObservableList<UserStock> userStocks) {
//		BigDecimal defaultThreshold = new BigDecimal(-1);
//		for (UserStock us : userStocks) {
//			BigDecimal valueThreshold = us.getValueThreshold();
//			BigDecimal curValueThreshold = us.getCurrentValueThreshold();
//			BigDecimal combinedThreshold = us.getCombinedValueThreshold();
//			BigDecimal curCombinedThreshold = us.getCurrentCombinedValueThreshold();
//			BigDecimal netProfitThreshold = us.getNetProfitThreshold();
//			BigDecimal curNetProfitThreshold = us.getCurrentNetProfitThreshold();
//			// Whenever a threshold reached, reset to default value
//			if (valueThreshold != null && curValueThreshold != null) {
////				if (isThresholdCrossed(valueThreshold, us.getStock().getPreviousPrice(), us.getStock().getPrice())) {
////					us.setValueThreshold(defaultThreshold);
////				}
//			}
//			if (combinedThreshold != null && curCombinedThreshold != null && combinedThreshold.compareTo(curCombinedThreshold) < 0) {
//				us.setCombinedValueThreshold(defaultThreshold);
//			}
//			if (netProfitThreshold != null && curNetProfitThreshold != null && netProfitThreshold.compareTo(curNetProfitThreshold) < 0) {
//				us.setNetProfitThreshold(defaultThreshold);
//			}
//			userStockManager.update(us);
//		}
//	}
	

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
