package main.java.control;

import java.sql.Date;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import main.java.model.Account;
import main.java.model.User;
import main.java.utility.PhoneNumberFormatter;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

public class RegistrationViewController extends BaseController {
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
			String phoneNumber = phoneNumberTF.getText().replaceAll("-", "");
			Date birthday = Date.valueOf(dateOfBirthDP.getValue());
			User user = new User(username, password, firstName, lastName, email, phoneNumber, birthday);
			// Create account for new user
			Account account = new Account(user, 1000, user.getFirstName() + " " + user.getLastName(), null);
			user.setAccount(account);
			account.setUser(user);
			userManager.add(user);
			// Register successfully. Switch to Login page
			Thread.sleep(2000);
			switchScreen(registerGP, Screen.LOGIN);
		}
	}
	
	@FXML private void back(MouseEvent e) {
		switchScreen(registerGP, Screen.LOGIN);
	}
	
	private boolean validateUserInput() {
		return ValidationUtil.validateFirstName(firstNameTF, firstNameError)
		        && ValidationUtil.validateLastName(lastNameTF, lastNameError)
		        && ValidationUtil.validateUsername(usernameTF, usernameError)
		        && ValidationUtil.validateOriginalPassword(passwordPF, confirmPasswordError)
		        && ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, confirmPasswordError) 
		        && ValidationUtil.validateEmail(emailTF, emailError) 
		        && ValidationUtil.validateDoB(dateOfBirthDP, dobError)
				&& ValidationUtil.validatePhoneNumber(phoneNumberTF);
	}

	@Override
	protected void makeNewStage(Screen target, String stageTitle, String url) {
		// TODO Auto-generated method stub
		
	}
}
