/**
 * 
 */
package main.java.utility;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import main.java.model.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

/**
 * @author aqd14
 *
 */
public class Utility {
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern VALID_CHARACTERS_REGEX = Pattern.compile("^[a-zA-Z]+$");

	/**
	 * 
	 */
	public Utility() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get list of big stocks
	 * @return stocks
	 * @throws IOException 
	 */
	public static ObservableList<Stock> getMultipleStockData(String[] symbols) throws IOException {
		if (symbols == null || symbols.length <= 0) {
			System.err.println("Stock list is incorrect!");
			return null;
		}
		Map<String, yahoofinance.Stock> stocksMap = YahooFinance.get(symbols, true);
		ObservableList<Stock> stocks = extractStockData(stocksMap);
		return stocks;
	}
	
	public static ObservableList<Stock> extractStockData(Map<String, yahoofinance.Stock> stocksMap) throws IOException {
		ObservableList<Stock> stocks = FXCollections.observableArrayList();
		for (Map.Entry<String, yahoofinance.Stock> entry : stocksMap.entrySet()) {
			Stock stock = new Stock();
			stock.setStockCode(entry.getKey());
			yahoofinance.Stock s = entry.getValue();
			s.print();
			// Extract stock information
			stock.setStockName(s.getName());
			StockQuote stockQuote = s.getQuote(true);
			if (stockQuote != null) {
				if (stockQuote.getPrice() != null)
					stock.setPrice(stockQuote.getPrice().doubleValue());
				if (stockQuote.getPreviousClose() != null)
					stock.setPreviousPrice(stockQuote.getPreviousClose().doubleValue());
			}
			stocks.add(stock);
		}
		return stocks;
	}
	
	/**
	 * Verify if the given text field is empty or not
	 * @param tf Given TextField instance
	 * @return True if not empty, otherwise returns False
	 */
	public static boolean isTextFieldEmpty(TextField tf) {
		if (tf == null) {
			return true;
		}
		return tf.getText().equals("");
	}
	
	/**
	 * Validate email address by using regular expression
	 * @param email
	 * @return True if email is valid, otherwise return False
	 */
	public static boolean isValidEmail(String email) {
	        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
	        return matcher.find();
	}
	
	/**
	 * User's name should only contains alphabetical characters
	 * @param instance
	 * @return
	 */
	public static boolean isNameValid(String instance) {
		Matcher matcher = VALID_CHARACTERS_REGEX.matcher(instance);
		return matcher.find();
	}
}
