/**
 * 
 */
package main.java.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
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
	
	/** Store username to avoid user from entering username or email whenever user:
	 *  	+ selects [Remember Me]
	 * 		+ finishes Registration
	 * 		+ reset password
	 */
	public static final String REMEMBER_ME_FILE = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "main" + File.separator + "resources" + File.separator + "remember_me.txt";

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
			// Extract stock information
			stock.setStockName(s.getName());
			StockQuote stockQuote = s.getQuote(true);
			if (stockQuote != null) {
				if (stockQuote.getPrice() != null)
					stock.setPrice(stockQuote.getPrice());
				if (stockQuote.getPreviousClose() != null)
					stock.setPreviousPrice(stockQuote.getPreviousClose());
				if (stockQuote.getChange() != null)
					stock.setPriceChange(stockQuote.getChange());
				if (stockQuote.getChangeInPercent() != null)
					stock.setPriceChangePercent(stockQuote.getChangeInPercent());
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
	
	// ---------------  READ AND WRITE FILE -------------------//
	
	/**
	 * Read first line in REMEMBER_ME file to get input username or email
	 * @return
	 */
	public static String readFile() {
		FileReader fileReader;
		try {
			fileReader = new FileReader(REMEMBER_ME_FILE);
			BufferedReader reader = new BufferedReader(fileReader);
		    String line = null;
		    line = reader.readLine();
	        reader.close();
	        return line;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		return "";
	}
	
	/**
	 * Write first line in REMEMBER_ME file to get input username or email
	 * @param content Updated username
	 */
	public static void writeFile(String content) {
		// Try with resources management
		// Don't need to close file anymore
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(REMEMBER_ME_FILE));){
		    writer.write(content);
	        writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    java.math.BigDecimal bd = new java.math.BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/**
	 * Format number as US Dollar currency
	 * @param number
	 * @return Formatted number. Returns "N/A" if current number is not available.
	 */
	public static String formatCurrencyNumber(Number number) {
		try {
			return NumberFormat.getNumberInstance(java.util.Locale.US).format(number);
		} catch (NullPointerException e1) {
			return "N/A";
		} catch (IllegalArgumentException e2) {
			return "N/A";
		}
	}
	
	public static String formatCurrencyDouble(double number) {
		DecimalFormat df = new DecimalFormat("####,###,###.00"); 
		return df.format(number);
	}
	
	public static Double parseCurrencyDouble(String number) {
		Double value = null;
		try {
			value = new Double(NumberFormat.getNumberInstance(java.util.Locale.US).parse(number.replace("$", "")).doubleValue());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return value;
	}
}
