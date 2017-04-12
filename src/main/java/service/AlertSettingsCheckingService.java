/**
 * 
 */
package main.java.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.UserStock;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
/**
 * <p>
 * Scheduled service to check if any threshold of Alert Settings crossed
 * Default checking time interval is 30 minutes. 
 * </p>
 * @author doquocanh-macbook
 *
 */
public class AlertSettingsCheckingService extends ScheduledService<ObservableList<UserStock>> {
	
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
					// Keep track of what thresholds crossed so that we can reset threshold in database
					boolean isValueThresholdCrossed = false;
					boolean isCombinedValueThresholdCrossed = false;
					boolean isNetProfitThresholdCrossed = false;
//					boolean thresholdCrossed = false;
					Stock stock = us.getStock();
					yahoofinance.Stock yahooStock = YahooFinance.get(stock.getStockCode());
					BigDecimal curPrice = yahooStock.getQuote().getPrice();
					// Check value threshold crossed
					if (us.getValueThreshold().compareTo(defaultThreshold) > 0) {
						isValueThresholdCrossed = isThresholdCrossed(us.getValueThreshold(), us.getStock().getPrice(), curPrice);
					}
					
					// Check combined value threshold crossed
					BigDecimal previousCombinedValue = calculatePreviousCombinedValueThreshold(yahooStock);
					BigDecimal curCombinedValue = calculateCurrentCombinedValueThreshold(yahooStock);
					if (us.getCombinedValueThreshold().compareTo(defaultThreshold) > 0) {
						isCombinedValueThresholdCrossed = isThresholdCrossed(us.getCombinedValueThreshold(), previousCombinedValue, curCombinedValue);
					}
					
					// Check net gain/net loss
					if (us.getNetProfitThreshold().compareTo(defaultThreshold) > 0) {
						isNetProfitThresholdCrossed = isNetThresholdCrossed(us.getNetProfitThreshold(), previousCombinedValue, curCombinedValue);
					}
					
					// Set threshold values for display purpose later
					us.setCurrentValueThreshold(curPrice);
//					us.setCurrentCombinedValueThreshold(curr);
					
					// Reset threshold in database when any threshold got crossed
					if (isValueThresholdCrossed || isCombinedValueThresholdCrossed || isNetProfitThresholdCrossed) {
						// Reset value threshold
						if (isValueThresholdCrossed) {
							us.setValueThreshold(defaultThreshold);
						}
						// Reset combined value threshold
						if (isCombinedValueThresholdCrossed) {
							us.setCombinedValueThreshold(defaultThreshold);
						}
						// Reset net gain/loss threshold
						if (isNetProfitThresholdCrossed) {
							us.setNetProfitThreshold(defaultThreshold);
						}
						// Update database
						usManager.update(us);
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
	 * @param previous		The price at the moment user settings
	 * @param curPrice			Current price of stock
	 * @return	<code>True</code> if the price went below or above threshold. Otherwise, returns <code>False</code> 
	 */
	private boolean isThresholdCrossed(BigDecimal threshold, BigDecimal previous, BigDecimal curPrice) {
		// At the time user set threshold, the value of threshold is lesser than the stock price
		// So, we check if the current price went down below threshold or not
		curPrice = new BigDecimal(1000);
		if (threshold.compareTo(previous) < 0) {
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
	 * 
	 * @param threshold
	 * @param previousPrice
	 * @param curPrice
	 * @return
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
}
