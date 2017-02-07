package main.java.control;

import javafx.scene.text.Text;

public class ParentController {

	public ParentController() {
		// TODO Auto-generated constructor stub
	}
	
	protected void displayErrorMessage(Text instance, String errMessage) {
		instance.setText(errMessage);
		instance.setVisible(true);
	}
	
	protected void hideErrorMessage(Text instance) {
		instance.setVisible(false);
	}
}
