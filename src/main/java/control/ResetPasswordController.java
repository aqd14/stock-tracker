package main.java.control;

import java.io.IOException;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.dao.UserManager;
import main.java.model.User;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

public class ResetPasswordController extends BaseController implements IController {
	@FXML private AnchorPane resetPasswordAP;
	@FXML private JFXTextField usernameTF;
	@FXML private JFXPasswordField passwordPF;
	@FXML private JFXPasswordField confirmPasswordPF;
	@FXML private Text errorT;
	
	public ResetPasswordController() {
		// TODO Auto-generated constructor stub
	}
	
	@FXML protected void back(MouseEvent e) {
		switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, "../../../main/java/view/Login.fxml");
	}
	
	@FXML protected void reset(MouseEvent e) {
		System.out.println("Reset password.");
		if (ValidationUtil.validateOriginalPassword(passwordPF, errorT) &&
			ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, errorT)) {
			UserManager<User> userManager = new UserManager<User>();
			User user = userManager.findByUsernameOrEmail(usernameTF.getText(), null);
			if (user == null) {
				ValidationUtil.displayErrorMessage(errorT, CommonDefine.USER_NOT_EXIST);
				return;
			}
			user.setPassword(passwordPF.getText());
			userManager.update(user);
			// Update remember_me file
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public void switchScreen(Screen target, String title, String url) {
        Parent root = null;
        try {
			switch (target) {
				case LOGIN:
					root = new FXMLLoader(getClass().getResource(url)).load();
					break;
				default:
					return;
			}
        } catch (IOException e) {
        	e.printStackTrace();
        	return;
		}
    	Stage curStage = (Stage)resetPasswordAP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
	}

	@Override
	public void makeNewStage(Screen target, String title, String url) {
		// TODO Auto-generated method stub
		
	}
}
