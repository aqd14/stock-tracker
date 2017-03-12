package main.java.control;

import main.java.dao.StockManager;
import main.java.dao.TransactionManager;
import main.java.dao.UserManager;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.User;
import main.java.model.UserStock;

public class BaseController {
	
	protected User user;
	protected UserManager<User> userManager;
	protected StockManager<Stock> stockManager;
	protected UserStockManager<UserStock> userStockManager;
	protected TransactionManager<Transaction> transactionManager;
	
	public BaseController() {
		// TODO Auto-generated constructor stub
		 userManager = new UserManager<User>();
		 stockManager = new StockManager<Stock>();
		 userStockManager = new UserStockManager<UserStock>();
		 transactionManager = new TransactionManager<Transaction>();
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
