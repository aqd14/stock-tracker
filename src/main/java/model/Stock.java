package main.java.model;
// default package
// Generated Feb 9, 2017 11:38:22 PM by Hibernate Tools 5.2.0.CR1

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

/**
 * Stock generated by hbm2java
 */
public class Stock extends RecursiveTreeObject<Stock> implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Transaction transaction;
	private String stockName;
	private String stockCode;
	private BigDecimal price;
	private BigDecimal previousPrice;
	private int amount;
	private Set<UserStock> userStocks = new HashSet<UserStock>(0);
	private StockDetail stockDetail;
	
	// Only for printing out purpose
	private BigDecimal priceChange;
	private BigDecimal priceChangePercent;
	
//	private StringProperty stockBuy;
	public Stock() {
	}

	public Stock(Transaction transaction, String stockName, String stockCode, int amount, BigDecimal price, BigDecimal previousPrice) {
		this.transaction = transaction;
		this.stockName = stockName;
		this.stockCode = stockCode;
		this.amount = amount;
		this.price = price;
		this.previousPrice = previousPrice;
	}

	public Stock(Transaction transaction, String stockName, String stockCode, int amount, BigDecimal price, BigDecimal previousPrice,
	        Set<UserStock> userStocks, StockDetail stockDetail) {
		this(transaction, stockName, stockCode, amount, price, previousPrice);
		this.stockDetail = stockDetail;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Transaction getTransaction() {
		return this.transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public String getStockName() {
		return this.stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getStockCode() {
		return this.stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
//		price = Utility.round(price, 2);
		// Rounding to 2 decimal numbers
		price = price.setScale(2, RoundingMode.CEILING);
		this.price = price;
	}

	public BigDecimal getPreviousPrice() {
//		price = Utility.round(price, 2);
		return this.previousPrice;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setPreviousPrice(BigDecimal previousPrice) {
		previousPrice = previousPrice.setScale(2, RoundingMode.CEILING);
		this.previousPrice = previousPrice;
	}

	public Set<UserStock> getUserStocks() {
		return this.userStocks;
	}

	public void setUserStocks(Set<UserStock> userStocks) {
		this.userStocks = userStocks;
	}

	public StockDetail getStockDetail() {
		return this.stockDetail;
	}

	public void setStockDetail(StockDetail stockDetail) {
		this.stockDetail = stockDetail;
	}
	
	// Used to print out in stock list
	
	public String getPriceString() {
		return Double.toString(price.doubleValue()); //price.toString();
	}
	
	public String getPreviousPriceString() {
		return Double.toString(previousPrice.doubleValue()); //previousPrice.toString();
	}
	
	public void setPriceChange(BigDecimal priceChange) {
		this.priceChange = priceChange;
	}
	
	public BigDecimal getPriceChange() {
		return priceChange;
	}
	
	public String getPriceChangeString() {
		return Double.toString(priceChange.doubleValue()); // priceChange.toString();
	}
	
	public void setPriceChangePercent(BigDecimal priceChangePercent) {
		priceChangePercent.setScale(2, RoundingMode.CEILING);
		this.priceChangePercent = priceChangePercent;
	}
	
	public BigDecimal getPriceChangePercent() {
		return priceChangePercent;
	}
	
	public String getPriceChangePercentString() {
		return Double.toString(priceChangePercent.doubleValue()); // priceChangePercent.toString();
	}
	
//	public StringProperty getStockBuy() {
//		return this.stockBuy;
//	}
	
//	public void setStockBuy(String stockBuy) {
//		this.stockBuy = new SimpleStringProperty(stockBuy);
//	}
}
