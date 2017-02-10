package main.java.control;

import java.io.IOException;
import java.sql.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.err.ErrorMessage;
import main.java.model.Account;
import main.java.model.User;
import main.java.utility.Utility;
import main.java.utility.WindowSize;

public class RegistrationViewController extends ParentController{
	// TextField objects for user information
	@FXML private TextField firstNameTF;
	@FXML private TextField lastNameTF;
	@FXML private TextField usernameTF;
	@FXML private PasswordField passwordPF;
	@FXML private PasswordField confirmPasswordPF;
	@FXML private TextField emailTF;
	@FXML private DatePicker dateOfBirthDP;
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
	@FXML public void handleRegisterSubmit(ActionEvent registerEvent) throws InterruptedException {
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
			switchRegisterToLogin();
		}
	}
	
	private boolean validateUserInput() {
		return validateFirstName() && validateLastName() && 
				validateUsername() && validatePassword() && 
				validateEmail() && validateDoB();
	}
	
	/**
	 * Validate user's first name.
	 * It shouldn't be empty. It shouldn't have special characters (!,@,#,$,%,&,*..)
	 */
	private boolean validateFirstName() {
		// Validate first name's empty
		if (Utility.isTextFieldEmpty(firstNameTF)) {
			displayErrorMessage(firstNameError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!Utility.isNameValid(firstNameTF.getText())) {
			displayErrorMessage(firstNameError, ErrorMessage.INVALID_NAME_ERR);
			return false;
		}
		// Valid first name
		hideErrorMessage(firstNameError);
		return true;
	}
	
	/**
	 * Last name is validate through several phases.
	 * If it fails in one phase, the error message corresponding to
	 * that phase will be displayed. Function stops validating.
	 */
	private boolean validateLastName() {
		// Validate first name
		if (Utility.isTextFieldEmpty(lastNameTF)) {
			displayErrorMessage(lastNameError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!Utility.isNameValid(lastNameTF.getText())) {
			displayErrorMessage(lastNameError, ErrorMessage.INVALID_NAME_ERR);
			return false;
		}
		
		// Passed all phases, set error text invisible
		hideErrorMessage(lastNameError);
		return true;
	}
	
	private boolean validateUsername() {
		// Validate first name
		if (Utility.isTextFieldEmpty(usernameTF)) {
			displayErrorMessage(usernameError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		}
		
		boolean userAlreadyExisted = false;
		if (userAlreadyExisted) {
			// Search through all database to find if user already existed
			// Print warning message
			return false;
		}
		
		// Check if username contains invalid characters
		boolean isContainedInvalidChar = false; 
		if (isContainedInvalidChar) {
			// Print warning message
			return false;
		}
		
		final int minLength = 6;
		final int maxLength = 15;
		if (usernameTF.getText().length() < minLength || usernameTF.getText().length() > maxLength) { // Length should be from 6 - 30 (Eg)
			// Print warning message
			displayErrorMessage(usernameError, ErrorMessage.INVALID_USERNAME_LENGTH_ERR);
			return false;
		}
		hideErrorMessage(usernameError);
		return true;
	}
	
	private boolean validateOriginalPassword() {
		// Validate empty
		if (Utility.isTextFieldEmpty(passwordPF)) {
			passwordError.setText(ErrorMessage.EMPTY_FIELD_ERR);
			passwordError.setVisible(true);
			return false;
		}
		
		final int minLength = 8;
		if(passwordPF.getText().length() < minLength) {
			displayErrorMessage(passwordError, ErrorMessage.PASSWORD_TOO_SHORT_ERR);
			return false;
		}
		// Passed all validation
		hideErrorMessage(passwordError);
		return true;
	}
	
	private boolean validateConfirmedPassword() {
		if (Utility.isTextFieldEmpty(confirmPasswordPF)) {
			displayErrorMessage(confirmPasswordError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!passwordPF.getText().equals(confirmPasswordPF.getText())) {
			System.err.println("Passwords not matching!");
			displayErrorMessage(confirmPasswordError, ErrorMessage.PASSWORD_NOT_MATCHED_ERR);
			return false;
		}
		// Passed all validation
		hideErrorMessage(confirmPasswordError);
		return true;
	}
	
	private boolean validatePassword() {
		return validateOriginalPassword() && validateConfirmedPassword();
	}
	
	/**
	 * Validate email address:
	 * <p><ul>
	 * 	<li> Is email empty?
	 *  <li> Is email valid (contains only alphabets, underscore, and period)
	 *  <li> Is email already existing on database?
	 *  <ul><p>
	 */
	private boolean validateEmail() {
		if (Utility.isTextFieldEmpty(emailTF)) {
			displayErrorMessage(emailError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!Utility.isValidEmail(emailTF.getText())) {
			displayErrorMessage(emailError, ErrorMessage.INVALID_EMAIL_ERR);
			return false;
		}
		
		User user = userManager.findByEmail(emailTF.getText());
		if (null != user) {
			displayErrorMessage(emailError, ErrorMessage.EMAIL_TAKEN_ERR);
			return false;
		}
		// Passed all validations
		hideErrorMessage(emailError);
		return true;
	}
	
	private boolean validateDoB() {
		if (null == dateOfBirthDP.getValue()) {
			displayErrorMessage(dobError, ErrorMessage.EMPTY_FIELD_ERR);
			return false;
		}
		hideErrorMessage(dobError);
		return true;
	}
	
	/**
	 * Switch from Registration page to Login page
	 */
	private void switchRegisterToLogin() {
    	Stage curStage = (Stage)registerGP.getScene().getWindow();
        Parent login;
		try {
			login = new FXMLLoader(getClass().getResource("../../../main/java/view/Login.fxml")).load();
	        curStage.setTitle("User Registration");
	        curStage.setScene(new Scene(login, WindowSize.LOGIN_WIDTH, WindowSize.LOGIN_HEIGHT));
	        curStage.setResizable(false);
	        curStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
