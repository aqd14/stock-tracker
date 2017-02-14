package main.java.control;

import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import main.java.utility.Screen;

public class ResetPasswordController extends ParentController {
	@FXML private AnchorPane resetPasswordAP;
	@FXML private JFXTextField usernameTF;
	@FXML private JFXTextField passwordTF;
	@FXML private JFXTextField confirmPasswordTF;
	
	public ResetPasswordController() {
		// TODO Auto-generated constructor stub
	}
	
	@FXML protected void back(MouseEvent e) {
		switchScreen(resetPasswordAP, Screen.LOGIN);
	}
	
	@FXML protected void reset(MouseEvent e) {
		System.out.println("Reset password.");
		
	}
}
