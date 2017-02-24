package main.java.control;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import main.java.dao.*;
import main.java.model.User;
import main.java.utility.Screen;

public class ParentController {
	
	protected User user;
	protected UserManager userManager;
	protected StockManager stockManager;
	protected UserStockManager userStockManager;
	protected TransactionManager transactionManager;
	
	public ParentController() {
		// TODO Auto-generated constructor stub
		 userManager = new UserManager();
		 stockManager = new StockManager();
		 userStockManager = new UserStockManager();
		 transactionManager = new TransactionManager();
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
	
//	public void setStage(Stage stage) {
//		this.stage = stage;
//	}
}
