package main.java.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertFactory {

	private AlertFactory() {
		// No constructor is needed
	}

	public static Alert generateAlert(AlertType type, String content) {
		Alert alert = new Alert(type);
//		alert.setTitle("Confirmation Dialog");
		alert.setContentText(content);
		return alert;
	}
}
