package main.java.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionWrapper {
	private Transaction transaction;
	private Stock stock;
	
	/**
	 * Wrapper class to have attributes from both Transaction and Stock 
	 */
	public TransactionWrapper() {
		// TODO Auto-generated constructor stub
	}
	
	public TransactionWrapper(Transaction transaction, Stock stock) {
		this.transaction = transaction;
		this.stock = stock;
	}
	
	public String getTransactionDate() {
		Date date = transaction.getTransactionDate();
		SimpleDateFormat fm = new SimpleDateFormat("MM/dd/yyyy");
		return fm.format(date);
	}
	
	public String getTransactionTime() {
		Date date = transaction.getTransactionDate();
		SimpleDateFormat fm = new SimpleDateFormat("HH:mm:ss");
		return fm.format(date);
	}
	
	public String getStockCode() {
		return stock.getStockCode();
	}
	
	public String getStockCompany() {
		return stock.getStockName();
	}
	
	public String getAmount() {
		return String.valueOf(stock.getAmount());
	}
	
	public String getPrice() {
		return stock.getPriceString();
	}
}
