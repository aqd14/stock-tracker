package main.java.control;

import java.sql.Date;

import com.jfoenix.controls.JFXDatePicker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import main.java.model.Account;
import main.java.model.User;
import main.java.utility.Screen;
import main.java.utility.ValidationUtil;

public class RegistrationViewController extends ParentController {
	// TextField objects for user information
	@FXML private TextField firstNameTF;
	@FXML private TextField lastNameTF;
	@FXML private TextField usernameTF;
	@FXML private PasswordField passwordPF;
	@FXML private PasswordField confirmPasswordPF;
	@FXML private TextField emailTF;
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
			Date birthday = Date.valueOf(dateOfBirthDP.getValue());
			User user = new User(username, password, firstName, lastName, email, birthday);
			// Create account for new user
			Account account = new Account(user, 0.0, user.getFirstName() + " " + user.getLastName(), null);
			user.setAccount(account);
			account.setUser(user);
			userManager.create(user);
			// Register successfully. Switch to Login page
			Thread.sleep(2000);
			switchScreen(registerGP, Screen.LOGIN);
		}
	}
	
	@FXML private void back(MouseEvent e) {
		switchScreen(registerGP, Screen.LOGIN);
	}
	
	private boolean validateUserInput() {
		return ValidationUtil.validateFirstName(firstNameTF, firstNameError) && ValidationUtil.validateLastName(lastNameTF, lastNameError) && 
				ValidationUtil.validateUsername(usernameTF, usernameError) && ValidationUtil.validateOriginalPassword(passwordPF, confirmPasswordError) && 
				ValidationUtil.validateConfirmedPassword(passwordPF, confirmPasswordPF, confirmPasswordError) && ValidationUtil.validateEmail(emailTF, emailError) && ValidationUtil.validateDoB(dateOfBirthDP, dobError);
	}
}
