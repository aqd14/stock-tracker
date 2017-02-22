/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import yahoofinance.Stock;

/**
 * @author doquocanh-macbook
 *
 */
public class StockDetailsController extends ParentController implements Initializable {
	
	@FXML private Label companyLB;
	@FXML private Label stockCodeAndTimeLB;
	@FXML private Label currentPriceLB;
	@FXML private Label priceChangeLB;
	
	RealTimeUpdateService service;
	
	private yahoofinance.Stock yahooStock;
	/**
	 * 
	 */
	public StockDetailsController() {
		yahooStock = null;
	}

	public void setStock(yahoofinance.Stock stock) {
		this.yahooStock = stock;
		service.setStockCode(this.yahooStock.getSymbol());
		service.start();
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				System.out.println("Update new stock changes...");
				yahooStock = (Stock) event.getSource().getValue();
				// Update values in GUIs
				initContent();
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
		currentPriceLB.setText(yahooStock.getQuote().getPrice().toString());
	}
	
	private void initPriceChange() {
		// The difference between current price and price in last close
		double priceChangeInMoney = yahooStock.getQuote().getChange().doubleValue();
		// Calculate in double value
//		Double priceChangeInPercent = Utils.getPercent(yahooStock.getQuote().getPrice().doubleValue(), yahooStock.getQuote().getPreviousClose().doubleValue());
		double priceChangeInPercent = yahooStock.getQuote().getChangeInPercent().doubleValue();//toString().substring(1); // Remove sign (- or +)
		
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

	public void initContent() {
		initCompanyName();
		initStockCodeAndTime();
		initCurrentPrice();
		initPriceChange();
	}
	
	public void checkStockRealTime() {
		// Create a thread to check price change for every 2 minutes
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Check stock changes in real-time for each 2 minutes
		service = new RealTimeUpdateService();
		service.setPeriod(Duration.minutes(2));
	}
	
	private static class RealTimeUpdateService extends ScheduledService<yahoofinance.Stock> {
		private String stockCode;
		
		public void setStockCode(String stockCode) {
			this.stockCode = stockCode;
		}
		
		@Override
		protected Task<Stock> createTask() {
			return new Task<Stock>() {
				@Override
				protected Stock call() throws IOException {
					System.out.println("Start getting data stock: " + stockCode + " ...");
					return yahoofinance.YahooFinance.get(stockCode);
				}
			};
		}
		
	}
}
