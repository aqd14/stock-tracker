package main.java.utility;

import javafx.stage.Stage;

public class StageFactory {

	private StageFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static Stage generateStage(String title) {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setResizable(false);
		return stage;
	}
 
}
