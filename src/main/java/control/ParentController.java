package main.java.control;

import javafx.scene.text.Text;
import main.java.dao.UserManager;

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
	
//	public void setStage(Stage stage) {
//		this.stage = stage;
//	}
}
