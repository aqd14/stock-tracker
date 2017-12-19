package main.java.control;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.model.Account;
import main.java.model.User;
import main.java.utility.PhoneNumberFormatter;
import main.java.utility.ResourceLocator;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

import java.io.IOException;
import java.sql.Date;

public class RegistrationController extends BaseController implements IController {
	// TextField objects for user information
	@FXML private JFXTextField firstNameTF;
	@FXML private JFXTextField lastNameTF;
	@FXML private JFXTextField usernameTF;
	@FXML private JFXPasswordField passwordPF;
	@FXML private JFXPasswordField confirmPasswordPF;
	@FXML private JFXTextField emailTF;
	@FXML private JFXTextField phoneNumberTF;
	@FXML private JFXDatePicker dateOfBirthDP;
	
	// Text objects for displaying error message when user input is invalid
	@FXML private Text firstNameError;
	@FXML private Text lastNameError;
	@FXML private Text usernameError;
	@FXML private Text passwordError;
	@FXML private Text confirmPasswordError;
	@FXML private Text emailError;
	@FXML private Text phoneNumberError;
	@FXML private Text dobError;
	
	@FXML private GridPane registerGP;
	
/*	public RegistrationViewController() {
		userManager = new UserManager();
		user = new User();
	}*/
	
	public void setPhoneNumberFormatter() {
		// Set phone number formatter for text field
		TextFormatter<String> formatter = new TextFormatter<>(PhoneNumberFormatter::addPhoneNumberMask);
		phoneNumberTF.setTextFormatter(formatter);
	}
	
	/**
	 * Validate user input when user clicks on [Register] button
	 * 
	 * @param registerEvent
	 * @throws InterruptedException 
	 */
	@FXML private void register(ActionEvent registerEvent) throws InterruptedException {
		boolean isAllInfoValid = validateUserInput();
		if (isAllInfoValid) {
			// Extract user information to create new user
			String username = usernameTF.getText();
			String password = passwordPF.getText();
			String firstName = firstNameTF.getText();
			String lastName = lastNameTF.getText();
			String email = emailTF.getText();
			String phoneNumber = phoneNumberTF.getText();
			Date birthday = Date.valueOf(dateOfBirthDP.getValue());
			User user = new User(username, password, firstName, lastName, email, phoneNumber, birthday);
			// Create account for new user
			Account account = new Account(user, 1000, user.getFirstName() + " " + user.getLastName(), null);
			user.setAccount(account);
			account.setUser(user);
			userManager.add(user);
			// Register successfully. Switch to Login page
			Thread.sleep(2000);
			switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, ResourceLocator.LOGIN_VIEW);
		}
	}
	
	@FXML private void back(MouseEvent e) {
		switchScreen(Screen.LOGIN, CommonDefine.LOGIN_TITLE, ResourceLocator.LOGIN_VIEW);
	}
	
	private boolean validateUserInput() {
		return ValidationUtil.validateFirstName(firstNameTF, firstNameError)
		        && ValidationUtil.validateLastName(lastNameTF, lastNameError)
		        && ValidationUtil.validateUsername(usernameTF, usernameError)
		        && ValidationUtil.validateOriginalPassword(passwordPF, passwordError)
		        && ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, confirmPasswordError) 
		        && ValidationUtil.validateEmail(emailTF, emailError) 
				&& ValidationUtil.validatePhoneNumber(phoneNumberTF, phoneNumberError)
		        && ValidationUtil.validateDoB(dateOfBirthDP, dobError);
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
	  	Stage curStage = (Stage)registerGP.getScene().getWindow();
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
