package main.java.control;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import main.java.dao.UserManager;
import main.java.err.ErrorMessage;
import main.java.model.User;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

public class ResetPasswordController extends ParentController {
	@FXML private AnchorPane resetPasswordAP;
	@FXML private JFXTextField usernameTF;
	@FXML private JFXPasswordField passwordPF;
	@FXML private JFXPasswordField confirmPasswordPF;
	@FXML private Text errorT;
	
	public ResetPasswordController() {
		// TODO Auto-generated constructor stub
	}
	
	@FXML protected void back(MouseEvent e) {
		switchScreen(resetPasswordAP, Screen.LOGIN);
	}
	
	@FXML protected void reset(MouseEvent e) {
		System.out.println("Reset password.");
		if (ValidationUtil.validateOriginalPassword(passwordPF, errorT) &&
			ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, errorT)) {
			UserManager userManager = new UserManager();
			User user = userManager.findByUsernameOrEmail(usernameTF.getText(), null);
			if (user == null) {
				ValidationUtil.displayErrorMessage(errorT, ErrorMessage.USER_NOT_EXIST);
				return;
			}
			user.setPassword(passwordPF.getText());
			userManager.update(user);
			// Update remember_me file
		}
	}
}
