/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import main.java.model.*;

/**
 * @author doquocanh-macbook
 *
 */
public class StockDetailsController extends ParentController implements Initializable {
	
	@FXML private Label companyLB;
	@FXML private Label stockCodeAndTimeLB;
	@FXML private Label currentPriceLB;
	@FXML private Label priceChangeLB;
	
	@FXML private AnchorPane lineChartAP;
	@FXML private LineChart<CategoryAxis, NumberAxis> stockLineChart;
	
	RealTimeUpdateService service;
	
	// Buy Stock
	@FXML private Label stockCodeLB;
	@FXML private Label buyPriceLB;
	@FXML private JFXComboBox<Integer> quantityCB;
	@FXML JFXTextField currentBalanceTF;
	@FXML JFXTextField subTotalTF;
	@FXML JFXTextField remainBalanceTF;
	
	private yahoofinance.Stock yahooStock;
	/**
	 * 
	 */
	public StockDetailsController() {
		yahooStock = null;
	}

	public void setStock(yahoofinance.Stock stock) {
		this.yahooStock = stock;
		// Real-time update starts when user select a Stock
		service.setStockCode(this.yahooStock.getSymbol());
		service.start();
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				System.out.println("Update new stock changes...");
				yahooStock = (yahoofinance.Stock) event.getSource().getValue();
				// Update values in GUIs
				updateStockData();
			}
		});
	}
	
	public yahoofinance.Stock getYahooStock() {
		return yahooStock;
	}
	
	private void initCompanyName() {
		companyLB.setText(yahooStock.getName());
	}
	
	private void initStockCodeAndTime() {
		// Get current datetime and format it
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm a");
		Date date = new Date();
		// Build final string to display on the screen
		StringBuilder sb = new StringBuilder("");
		sb.append(yahooStock.getSymbol()).append(" - ").append(dateFormat.format(date));
		String finalDisplay = sb.toString(); //yahooStock.getSymbol() + " - " + dateFormat.format(date);
		// Set text to display
		stockCodeAndTimeLB.setText(finalDisplay);
	}
	
	private void initCurrentPrice() {
		currentPriceLB.setText(yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).toString());
	}
	
	private void initPriceChange() {
		// The difference between current price and price in last close
		double priceChangeInMoney = yahooStock.getQuote().getChange().setScale(2, RoundingMode.CEILING).doubleValue();
		// Calculate in double value
//		Double priceChangeInPercent = Utils.getPercent(yahooStock.getQuote().getPrice().doubleValue(), yahooStock.getQuote().getPreviousClose().doubleValue());
		double priceChangeInPercent = yahooStock.getQuote().getChangeInPercent().setScale(2, RoundingMode.CEILING).doubleValue();//toString().substring(1); // Remove sign (- or +)
		
		StringBuilder sb = new StringBuilder("");
		sb.append(priceChangeInMoney).append(" (").append(priceChangeInPercent).append("%)");
		String finalDisplay = sb.toString(); //yahooStock.getSymbol() + " - " + dateFormat.format(date);
		// Set text to display
		priceChangeLB.setText(finalDisplay);
		
		// Set text color base on changes in price
		BigDecimal priceChange = yahooStock.getQuote().getChange();
		if (priceChange.compareTo(BigDecimal.ZERO) == 0) { // Nothing change in price
			priceChangeLB.setTextFill(Color.BLACK);
		} else if (priceChange.compareTo(BigDecimal.ZERO) > 0) { // price increased
			priceChangeLB.setTextFill(Color.BLUE);
		} else { // Price decreased
			priceChangeLB.setTextFill(Color.RED);
		}
	}

	public void updateStockData() {
		initCompanyName();
		initStockCodeAndTime();
		initCurrentPrice();
		initPriceChange();
		// Make line chart based on current data
		try {
			drawLineChart(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Buying stock options
		stockCodeLB.setText(yahooStock.getSymbol());
		buyPriceLB.setText(yahooStock.getQuote().getPrice().toString());
		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		quantityCB.setItems(options);
		currentBalanceTF.setText("$ " + user.getAccount().getBalance());
	}
	
	/**
	 * <p>
	 * Generate line chart based on current stock and time interval.
	 * The list of current time interval is WEEK, MONTH, QUARTER and YEAR
	 * </p>
	 * 
	 * @param interval The time interval expected to show price trending of stock.
	 * The possible values of <code>interval</code> are: 
	 * <li>1 : WEEK</li>
	 * <li>2 : MONTH</li>
	 * <li>3 : QUATER</li>
	 * <li>4 : YEAR</li>
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void drawLineChart(int interval) throws IOException {
		if (yahooStock != null) {
			// Remove data from last time
			stockLineChart.getData().clear();
			
	        stockLineChart.getXAxis().setAutoRanging(true);
	        stockLineChart.getYAxis().setAutoRanging(true);
			
			List<HistoricalQuote> historyQuotes;
			// Get stock history within one year
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			
			switch(interval) {
				case 1: // Nearest week
					from.add(Calendar.DAY_OF_MONTH, -7);
					break;
				case 2: // Nearest month
					from.add(Calendar.MONTH, -1);
					break;
					
				case 3: // Nearest 3 months
					from.add(Calendar.MONTH, -3);
					break;
				case 4: //Nearest year
					from.add(Calendar.YEAR, -1);
					break;
				default:
					throw new RuntimeException("Wrong interval time: " + interval);
			}
			// Loading stock historical quotes
			System.out.println("From: " + from);
			System.out.println("From: " + to);
			historyQuotes = yahooStock.getHistory(from, to, Interval.DAILY);
			
			XYChart.Series series = new XYChart.Series();
			series.setName("Stock Price");
			// Reverse iteration because date appears as reverse order in List
			for (int index = historyQuotes.size() - 1; index >= 0; index --) {
				HistoricalQuote quote = historyQuotes.get(index);
				String date = String.valueOf(quote.getDate().get(Calendar.DATE));
				System.out.println(date);
				if (quote.getDate().get(Calendar.DATE) % 3 == 0) {
					series.getData().add(new XYChart.Data<String, Number>(date, quote.getOpen().doubleValue()));
				} else {
					series.getData().add(new XYChart.Data<String, Number>("", quote.getOpen().doubleValue()));
				}
//				System.out.println(quote);
				
				//series.getData().add(new XYChart<CategoryAxis, NumberAxis>().Data(String.valueOf(from.DATE), quote.getOpen()));
			}
	        stockLineChart.getData().add(series);
	        System.out.println("Data: " + stockLineChart.getData());
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Check stock changes in real-time for each 2 minutes
		service = new RealTimeUpdateService();
		service.setPeriod(Duration.minutes(2));
		// Auto change width of label based on current text length
		currentPriceLB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldVal, String newVal) {
				currentPriceLB.setPrefWidth(currentPriceLB.getText().length()*22 - 100/currentPriceLB.getText().length());
			}
		});
		
		// Add listener when user selects item in combobox
		quantityCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				System.out.println("Selected item: " + newValue);
				subTotalTF.setText("- $ " + yahooStock.getQuote().getPrice().doubleValue()*newValue);
				remainBalanceTF.setText("$ " + (user.getAccount().getBalance() - newValue*yahooStock.getQuote().getPrice().doubleValue()));
			}
			
		});
	}
	
	@FXML private void buyStock(ActionEvent e) {
		boolean isValidToBuy = true; // Condition to check if everything is correct to perform transaction
		if (isValidToBuy) {
			// Subtract money from user's balance
			double curBal= user.getAccount().getBalance();
			int quantity = quantityCB.getSelectionModel().getSelectedItem();
			double price = yahooStock.getQuote().getPrice().doubleValue();
			
			// Check if current balance is enough to buy stock
			double subtraction = curBal - quantity*price;
			if (subtraction > 0) {
				user.getAccount().setBalance(subtraction);
				userManager.update(user);
				// Create new instance and relationship in database
				Stock boughtStock = extractStock();
				stockManager.add(boughtStock);
				UserStockId userStockId = new UserStockId(boughtStock.getId(), user.getId());
				UserStock userStock = new UserStock(userStockId, boughtStock, user, -1.0, -1.0);
				userStockManager.add(userStock);
			} else {
				// Display error message for user
			}
		}
	}
	
	private Stock extractStock() {
		Transaction transaction = new Transaction(user.getAccount(), new Date());
		transactionManager.add(transaction);
		String stockName = yahooStock.getName();
		String stockCode = yahooStock.getSymbol();
		int amount = quantityCB.getSelectionModel().getSelectedItem();
		BigDecimal price = yahooStock.getQuote().getPrice();
		BigDecimal previousPrice = yahooStock.getQuote().getPreviousClose();
		Stock stock = new Stock(transaction, stockName, stockCode, amount, price, previousPrice);
		return stock;
	}
	
	private static class RealTimeUpdateService extends ScheduledService<yahoofinance.Stock> {
		private String stockCode;
		
		public void setStockCode(String stockCode) {
			this.stockCode = stockCode;
		}
		
		@Override
		protected Task<yahoofinance.Stock> createTask() {
			return new Task<yahoofinance.Stock>() {
				@Override
				protected yahoofinance.Stock call() throws IOException {
					System.out.println("Start getting data stock: " + stockCode + " ...");
					return yahoofinance.YahooFinance.get(stockCode);
				}
			};
		}
		
	}
}
