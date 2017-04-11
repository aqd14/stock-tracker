package main.java.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import main.java.dao.StockManager;
import main.java.dao.TransactionManager;
import main.java.dao.UserManager;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.TransactionWrapper;
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
	
	/**
	 * Update color based on value of cell
	 * 
	 * value > 0: Green
	 * value < 0: Red
	 * value = 0: Black
	 * 
	 * @param col
	 */
	protected void styleTableCell(TableColumn<TransactionWrapper, String> col) {
		col.setCellFactory(param -> new TableCell<TransactionWrapper, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				if (!empty) {
					super.updateItem(item, empty);
					item = item.replaceAll(",", "");
					StringBuilder bd = new StringBuilder();
					double value = Double.valueOf(item);
					// Price went down
					if (value < 0) {
						setTextFill(Color.RED);
						bd.append("- ").append(item.replace("-", ""));
					} else if (value > 0) { // Price went up
						setTextFill(Color.GREEN);
						bd.append("+ ").append(item);
					} else {
						// Price stays the same.
						// Reset to black
						setTextFill(Color.BLACK);
						bd.append(item);
					}
					setText(bd.toString());
				}
			}
		});
	}
}
