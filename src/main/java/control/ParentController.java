package main.java.control;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import main.java.dao.StockManager;
import main.java.dao.TransactionManager;
import main.java.dao.UserManager;
import main.java.dao.UserStockManager;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.User;
import main.java.model.UserStock;
import main.java.utility.Screen;

public abstract class ParentController {
	
	protected User user;
	protected UserManager<User> userManager;
	protected StockManager<Stock> stockManager;
	protected UserStockManager<UserStock> userStockManager;
	protected TransactionManager<Transaction> transactionManager;
	
	public ParentController() {
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
	
    protected void switchScreen(Region region, Screen targetScreen, User... users) {
    	Stage curStage = (Stage)region.getScene().getWindow();
        Parent root = null;
		try {
			switch (targetScreen) {
				case LOGIN:
					root = new FXMLLoader(getClass().getResource("../../../main/java/view/Login.fxml")).load();
					break;
				case HOME:
					FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../main/java/view/Home.fxml"));
					root = loader.load(); // Loading before get controller
					HomeController homeController = loader.<HomeController>getController();
					homeController.setUser(users[0]);
					break;
				case REGISTER:
					root = new FXMLLoader(getClass().getResource("../../../main/java/view/UserRegistration.fxml")).load();
					break;
				case RESET_PASSWORD:
					root = new FXMLLoader(getClass().getResource("../../../main/java/view/ResetPassword.fxml")).load();
					break;
				default:
					return;
			}
//	        curStage.setTitle("User Registration");
	        curStage.setScene(new Scene(root));
	        curStage.setResizable(false);
	        curStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	/**
	 * Create new stage besides primary one. That means there are more than one views displayed 
	 * on the screen.
	 * 
	 * @param target The view that user wants to switch to
	 * @param stageTitle The title of created stage
	 * @param url <code>URL</code> to FXML file
	 */
	protected abstract void makeNewStage(Screen target, String stageTitle, String url);
}
