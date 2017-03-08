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

import com.jfoenix.controls.JFXButton;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.common.CommonDefine.Interval;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.UserStock;
import main.java.model.UserStockId;
import main.java.utility.AlertGenerator;
import main.java.utility.Utility;
import yahoofinance.histquotes.HistoricalQuote;

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
	@FXML JFXButton buyStockButton;
	
	@FXML private Label dayLow;
	@FXML private Label dayHigh;
	@FXML private Label volume;
	@FXML private Label marketCapValue;
	@FXML private Label peRatio;
	@FXML private Label eps;
	
	// Options for displaying line chart
	@FXML private Label oneWeekLB;
	@FXML private Label oneMonthLB;
	@FXML private Label threeMonthLB;
	@FXML private Label sixMonthLB;
	@FXML private Label oneYearLB;
	
	private Label selectedLB;
	
	private yahoofinance.Stock yahooStock;
	private Interval interval;
	
	/**
	 * 
	 */
	public StockDetailsController() {
		yahooStock = null;
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
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer quantity) {
				if (quantity != null) {
					System.out.println("Selected item: " + quantity);
					double price = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue()*quantity;
					double remainingBalance = user.getAccount().getBalance() - price;
					// Disable [Buy] button if the remaining balance is negative
					buyStockButton.setDisable(remainingBalance < 0);
					// Set corresponding balance and sub total
					subTotalTF.setText("- $" + Utility.formatCurrencyDouble(price));
					remainBalanceTF.setText("$" + Utility.formatCurrencyDouble(remainingBalance));
				}
			}
		});
		
		// default interval for line chart data is one month
		interval = Interval.ONE_MONTH;
		
		// Add event listener for line chart options
		oneWeekLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_WEEK;
			handleOptionSelected(oneWeekLB);
		});

		oneMonthLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_MONTH;
			handleOptionSelected(oneMonthLB);
		});
		
		threeMonthLB.setOnMouseClicked(event -> {
			interval = Interval.THREE_MONTH;
			handleOptionSelected(threeMonthLB);
		});
		
		sixMonthLB.setOnMouseClicked(event -> {
			interval = Interval.SIX_MONTH;
			handleOptionSelected(sixMonthLB);
		});
		
		oneYearLB.setOnMouseClicked(event -> {
			interval = Interval.ONE_YEAR;
			handleOptionSelected(oneYearLB);
		});
	}
	
	/**
	 * Update selected label for line chart data and draw corresponding line chart
	 * @param lb The option user is switching to
	 */
	private void handleOptionSelected(Label lb) {
		if (lb != selectedLB) {
			setSelectedStyle(lb);
			removeSelectedStyle(selectedLB);
			drawLineChart(interval);
			selectedLB = lb;
		}
	}
	
	private void setSelectedStyle(Label lb) {
		if (lb != null) {
			lb.setUnderline(true);
			lb.setTextFill(Color.RED);
		}
	}
	
	private void removeSelectedStyle(Label lb) {
		if (lb != null) {
			lb.setUnderline(false);
			lb.setTextFill(Color.BLACK);
		}
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
			priceChangeLB.setTextFill(Color.GREEN);
		} else { // Price decreased
			priceChangeLB.setTextFill(Color.RED);
		}
	}
	
	private void initDayLow() {
		dayLow.setText(Utility.formatCurrencyNumber(yahooStock.getQuote().getDayLow()));
	}
	
	private void initDayHigh() {
		dayHigh.setText(Utility.formatCurrencyNumber(yahooStock.getQuote().getDayHigh()));
	}
	
	private void initVolume() {
		volume.setText(Utility.formatCurrencyNumber(new BigDecimal(yahooStock.getQuote().getVolume())));
	}
	
	private void initMarketCap() {
		marketCapValue.setText(Utility.formatCurrencyNumber(yahooStock.getStats().getMarketCap()));
	}
	
	private void initPriceEarnRatio() {
		peRatio.setText(Utility.formatCurrencyNumber(yahooStock.getStats().getPe()));
	}
	
	private void initEarnPerShare() {
		eps.setText(Utility.formatCurrencyNumber(yahooStock.getStats().getEps()));
	}
	
	/**
	 * Update stock data displayed in page
	 */
	public void updateStockData() {
		// Above
		initCompanyName();
		initStockCodeAndTime();
		initCurrentPrice();
		initPriceChange();
		// Below
		initDayLow();
		initDayHigh();
		initVolume();
		initMarketCap();
		initPriceEarnRatio();
		initEarnPerShare();
		// One month is default interval for line chart when
		// user first opens Stock Details page
		handleOptionSelected(oneMonthLB);
		
		// Buying stock options
		stockCodeLB.setText(yahooStock.getSymbol());
		buyPriceLB.setText(yahooStock.getQuote().getPrice().toString());
		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		quantityCB.setItems(options);
		currentBalanceTF.setText("$" + Utility.formatCurrencyDouble(user.getAccount().getBalance()));
	}
	
	/**
	 * <p>
	 * Generate line chart based on current stock and time interval.
	 * The list of current time interval is WEEK, MONTH, QUARTER and YEAR
	 * </p>
	 * 
	 * @param interval The time interval expected to show price trending of stock.
	 * The possible values of <code>interval</code> are: 
	 * <li>ONE_WEEK</li>
	 * <li>ONE_MONTH</li>
	 * <li>THREE_MONTH</li>
	 * <li>SIX_MONTH</li>
	 * <li>ONE_YEARYEAR</li>
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void drawLineChart(Interval interval) {
		if (yahooStock != null) {
			// Remove data from last time
			stockLineChart.getData().clear();
			
	        stockLineChart.getXAxis().setAutoRanging(true);
	        stockLineChart.getYAxis().setAutoRanging(true);
			
			List<HistoricalQuote> historyQuotes = null;
			// Get stock history within one year
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			SimpleDateFormat dfm;
			
			switch(interval) {
				case ONE_WEEK: // Nearest week
					from.add(Calendar.DAY_OF_MONTH, -7);
					dfm = new SimpleDateFormat("EEE");
					break;
				case ONE_MONTH: // Nearest month
					from.add(Calendar.MONTH, -1);
					dfm = new SimpleDateFormat("MMM dd");
					break;
				case THREE_MONTH: // Nearest 3 months
					from.add(Calendar.MONTH, -3);
					dfm = new SimpleDateFormat("MMM dd");
					break;
				case SIX_MONTH: // Nearest 6 months
					from.add(Calendar.MONTH, -6);
					dfm = new SimpleDateFormat("MMM");
					break;
				case ONE_YEAR: //Nearest year
					from.add(Calendar.YEAR, -1);
					dfm = new SimpleDateFormat("MMM yyyy");
					break;
				default:
					return;
			}
			// Loading stock historical quotes
			System.out.println("From: " + from);
			System.out.println("From: " + to);
			try {
				historyQuotes = yahooStock.getHistory(from, to, yahoofinance.histquotes.Interval.DAILY);
			} catch (IOException e) { // Exception happens, return without creating line chart
				e.printStackTrace();
				return;
			}
			
			XYChart.Series series = new XYChart.Series();
			series.setName("Stock Price");
			// Reverse iteration because date appears as reverse order in List
			for (int index = historyQuotes.size() - 1; index >= 0; index --) {
				HistoricalQuote quote = historyQuotes.get(index);
				String date = dfm.format(quote.getDate().getTime());
				double price = quote.getOpen().doubleValue();
				series.getData().add(new XYChart.Data<String, Number>(date, price));
			}
	        stockLineChart.getData().add(series);
	        System.out.println("Data: " + stockLineChart.getData());
		}
	}

	@FXML private void buyStock(ActionEvent e) {
		// Subtract money from user's balance
		double curBal= user.getAccount().getBalance();
		int quantity = quantityCB.getSelectionModel().getSelectedItem();
		double price = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue();
		
		// Check if current balance is enough to buy stock
		double subtraction = Utility.round(curBal - quantity*price, 2);
		if (subtraction > 0) {
			user.getAccount().setBalance(subtraction);
			userManager.update(user);
			// Create new instance and relationship in database
			Stock boughtStock = extractStock();
			stockManager.add(boughtStock);
			UserStockId userStockId = new UserStockId(boughtStock.getId(), user.getId());
			UserStock userStock = new UserStock(userStockId, boughtStock, user);
			userStockManager.add(userStock);
		} else {
			// Display error message for user
		}
		setupAfterBuyingStock();
	}
	
	/**
	 * Update GUI elements after buying stocks successfully
	 */
	private void setupAfterBuyingStock() {
		Alert alert = AlertGenerator.generateAlert(AlertType.INFORMATION, CommonDefine.BUY_STOCK_SUCCESSFUL_SMS);
		alert.showAndWait();
		// After 
		quantityCB.getSelectionModel().clearSelection();
		subTotalTF.setText("");
		remainBalanceTF.setText("");
		currentBalanceTF.setText("$" + Utility.formatCurrencyDouble(user.getAccount().getBalance()));
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
