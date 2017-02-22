/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import main.java.model.Stock;
import main.java.utility.Screen;
import main.java.utility.Utility;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController extends ParentController implements Initializable {
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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// List of 30 stocks that will be displayed in Home page
		final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT",
													"TM", "KO", "HPQ", "FB", "F", "MSFT", 
													"BRK-A", "AMZN", "XOM", "JPM", "WFC", "GE",
													"BAC", "T", "BABA", "PG", "CVX", "V",
													"VZ", "HD", "DIS", "INTC", "ORCL", "HSBC"};
		
		// Initialize new background thread to handle downloading data
		Task<ObservableList<Stock>> task = new Task<ObservableList<Stock>>() {
			
			@Override
			protected ObservableList<Stock> call() throws Exception {
				// Display spinner while loading data.
				ObservableList<Stock> stocks = Utility.getMultipleStockData(stockSymbols);
				return stocks;
			}
		};
		
		// When task succeeded
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {
				// Update view as all stock data downloaded
				ObservableList<Stock> stocks = task.getValue();
				TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
				stockTableView.setRoot(root);
				// Finish loading, hide spinner
				spinner.setVisible(false);
			}
		});
		
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				System.err.println("Task performed unsuccessfully!");
				spinner.setVisible(false);
			}
		});
		
		// Start new thread
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
		
		// Initialize GUI
		ObservableList<Stock> stocks = FXCollections.observableArrayList();
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
		// setCellFactoryStockBuy();
		
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
					TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
					System.out.println("Selected stock: " + item.getValue().getStockName());
				}
			}
			
		});
	}
	
	/**
	 * Create new stage besides primary one. That means there are more than one views displayed 
	 * on the screen.
	 * 
	 * @param target The view that user wants to switch to
	 * @param stageTitle The title of created stage
	 * @param url <code>URL</code> to FXML file
	 */
	public void makeNewStage(Screen target, String stageTitle, String url) {
		Stage settingsStage = new Stage();
		settingsStage.setTitle(stageTitle);
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
			case SETTINGS:
				// Set current user
				SettingsController settingsController = loader.<SettingsController>getController();
				settingsController.setUser(user);
				settingsController.setAccountName(user.getAccount().getAccountName());
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
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
				stockController.initContent();
				break;
			default:
				return;
		}
		
		settingsStage.setScene(new Scene(root));
		settingsStage.setResizable(false);
		settingsStage.show();
			
	}
	
	/**
	 * Open new screen when user select [Settings] on Menu bar
	 * @param event Capture the action user performed. 
	 */
	@FXML private void openAccountSettings(ActionEvent event) {
		makeNewStage(Screen.SETTINGS, "Settings", "../view/Settings.fxml");
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
		//setPriceFormatColumn(changeCol);
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangePercentString()));
		//setPriceFormatColumn(percentChangeCol);
	}
	
//	private void setCellFactoryStockBuy() {
//		stockBuyCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<Stock, String> param) ->{
//			if(stockBuyCol.validateValue(param)) 
//				return param.getValue().getValue().getStockBuy();
//			else 
//				return stockBuyCol.getComputedValue(param);
//		});
//		
//		stockBuyCol.setCellFactory((TreeTableColumn<Stock, String> param) -> new GenericEditableTreeTableCell<Stock, String>(new TextFieldEditorBuilder()));
//		stockBuyCol.setOnEditCommit((CellEditEvent<Stock, String> t)->{
//			((Stock) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).setStockBuy(t.getNewValue());
//		});
//	}
	
	/**
	 * Set format for stock price. Only two floating points displayed
	 * @param c
	 */
//	private void setPriceFormatColumn(TreeTableColumn<Stock, Double> c) {
//		c.setCellFactory(col -> new TreeTableCell<Stock, Double>() {
//	        @Override 
//	        public void updateItem(Double price, boolean empty) {
//	            //super.updateItem(price, empty);
//	            if (empty || price < 0.001) {
//	                setText("0.00");
//	            } else {
//	            	DecimalFormat doubleFormat = new DecimalFormat("+#,##0.000;-#");
//            		setText(doubleFormat.format(price));
//	            }
//	        }
//	    });
//	}
}
