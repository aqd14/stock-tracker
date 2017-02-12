package main.java.control;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.dao.UserManager;
import main.java.utility.Screen;

public class ParentController {
	
	protected UserManager userManager;
//	protected Stage stage;

	public ParentController() {
		// TODO Auto-generated constructor stub
		 userManager = new UserManager();
	}
	
	protected void displayErrorMessage(Text instance, String errMessage) {
		instance.setText(errMessage);
		instance.setVisible(true);
	}
	
	protected void hideErrorMessage(Text instance) {
		instance.setVisible(false);   
	}
    
    protected void switchScreen(Region region, Screen targetScreen) {
    	Stage curStage = (Stage)region.getScene().getWindow();
        Parent root = null;
		try {
			switch (targetScreen) {
				case LOGIN:
					root = new FXMLLoader(getClass().getResource("../../../main/java/view/Login.fxml")).load();
					break;
				case HOME:
					root = new FXMLLoader(getClass().getResource("../../../main/java/view/Home.fxml")).load();
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
