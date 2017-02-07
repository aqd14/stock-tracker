package main.java.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import main.java.err.ErrorMessage;
import main.java.utility.Utility;

public class RegistrationViewController extends ParentController{
	@FXML private TextField firstName;
	@FXML private TextField lastName;
	@FXML private TextField username;
	@FXML private PasswordField password;
	@FXML private PasswordField confirmPassword;
	@FXML private TextField email;
	@FXML private DatePicker dateOfBirth;
	
	@FXML private Text firstNameError;
	@FXML private Text lastNameError;
	@FXML private Text usernameError;
	@FXML private Text passwordError;
	@FXML private Text confirmPasswordError;
	@FXML private Text emailError;
	@FXML private Text dobError;
	
	public RegistrationViewController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Validate user input when user clicks on [Register] button
	 * 
	 * @param registerEvent
	 */
	@FXML public void handleRegisterSubmit(ActionEvent registerEvent) {
//		System.out.println(password.getText());
//		System.out.println(dateOfBirth.getValue());
		validateUserInput();
	}
	
	private void validateUserInput() {
    	System.out.println(Utility.isNameValid("ABC"));
    	System.out.println(Utility.isNameValid("ABC!"));
		validateFirstName();
		validateLastName();
		validateUsername();
		validatePassword();
		validateEmail();
		validateDoB();
	}
	
	/**
	 * Validate user's first name.
	 * It shouldn't be empty. It shouldn't have special characters (!,@,#,$,%,&,*..)
	 */
	private void validateFirstName() {
		// Validate first name's empty
		if (Utility.isTextFieldEmpty(firstName)) {
			displayErrorMessage(firstNameError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		} 
		
		if (!Utility.isNameValid(firstName.getText())) {
			displayErrorMessage(firstNameError, ErrorMessage.INVALID_NAME_ERR);
			return;
		}
		
		hideErrorMessage(firstNameError);
		// Validate first name contains special characters
	}
	
	/**
	 * Last name is validate through several phases.
	 * If it fails in one phase, the error message corresponding to
	 * that phase will be displayed. Function stops validating.
	 */
	private void validateLastName() {
		// Validate first name
		if (Utility.isTextFieldEmpty(lastName)) {
			displayErrorMessage(lastNameError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		} 
		
		if (!Utility.isNameValid(lastName.getText())) {
			displayErrorMessage(lastNameError, ErrorMessage.INVALID_NAME_ERR);
			return;
		}
		
		// Passed all phases, set error text invisible
		hideErrorMessage(lastNameError);
	}
	
	private void validateUsername() {
		// Validate first name
		if (Utility.isTextFieldEmpty(username)) {
			displayErrorMessage(usernameError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		}
		
		boolean userAlreadyExisted = false;
		if (userAlreadyExisted) {
			// Search through all database to find if user already existed
			// Print warning message
		}
		
		// Check if username contains invalid characters
		boolean isContainedInvalidChar = false; 
		if (isContainedInvalidChar) {
			// Print warning message
		}
		
		boolean isInvalidLength = false;
		if (isInvalidLength) { // Length should be from 8 - 12 (Eg)
			// Print warning message
		}
		hideErrorMessage(usernameError);
	}
	
	private void validateOriginalPassword() {
		// Validate empty
		if (Utility.isTextFieldEmpty(password)) {
			passwordError.setText(ErrorMessage.EMPTY_FIELD_ERR);
			passwordError.setVisible(true);
			return;
		}
		
		if(password.getText().length() < 8) {
			displayErrorMessage(passwordError, ErrorMessage.PASSWORD_TOO_SHORT_ERR);
			return;
		}
		hideErrorMessage(passwordError);
	}
	
	private void validateConfirmedPassword() {
		if (Utility.isTextFieldEmpty(confirmPassword)) {
			displayErrorMessage(confirmPasswordError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		}
		
		if (!password.getText().equals(confirmPassword.getText())) {
			System.err.println("Passwords not matching!");
			displayErrorMessage(confirmPasswordError, ErrorMessage.PASSWORD_NOT_MATCHED_ERR);
			return;
		}
		
		hideErrorMessage(confirmPasswordError);
	}
	
	private void validatePassword() {
		validateOriginalPassword();
		validateConfirmedPassword();
	}
	
	/**
	 * Validate email address:
	 * <p><ul>
	 * 	<li> Is email empty?
	 *  <li> Is email valid (contains only alphabets, underscore, and period)
	 *  <li> Is email already existing on database?
	 *  <ul><p>
	 */
	private void validateEmail() {
		if (Utility.isTextFieldEmpty(email)) {
			displayErrorMessage(emailError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		}
		
		if (!Utility.isValidEmail(email.getText())) {
			displayErrorMessage(emailError, ErrorMessage.INVALID_EMAIL_ERR);
			return;
		}
		
		boolean isEmailAlreadyExisted = true;
		if (isEmailAlreadyExisted) {
			// print warning message
			// return;
		}
		// Passed all validations
		hideErrorMessage(emailError);
	}
	
	private void validateDoB() {
		if (null == dateOfBirth.getValue()) {
			displayErrorMessage(dobError, ErrorMessage.EMPTY_FIELD_ERR);
			return;
		}
		hideErrorMessage(dobError);
	}
}
