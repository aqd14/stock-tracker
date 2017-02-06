package main.java.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import main.java.err.ErrorMessage;
import main.java.utility.Utility;

public class RegistrationViewController {
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
	@FXML private Text emmailError;
	@FXML private Text bodError;
	
	public RegistrationViewController() {
		// TODO Auto-generated constructor stub
	}
	
	@FXML public void handleRegisterSubmit(ActionEvent registerEvent) {
//		System.out.println(password.getText());
//		System.out.println(dateOfBirth.getValue());
		validateFirstName();
	}
	
	private void validateFirstName() {
		// Validate first name
		if (Utility.isEmptyTextField(firstName)) {
			firstNameError.setText(ErrorMessage.EMPTY_FIELD_SMS);
			firstNameError.setVisible(true);
		} else {
			firstNameError.setVisible(false);
		}
	}
	
	private void validateLastName() {
		// Validate first name
		if (Utility.isEmptyTextField(lastName)) {
			lastNameError.setText(ErrorMessage.EMPTY_FIELD_SMS);
			lastNameError.setVisible(true);
		} else {
			lastNameError.setVisible(false);
		}
	}
	
	private void validateUsername() {
		// Validate first name
		if (Utility.isEmptyTextField(username)) {
			// Print warning message
		}
		
		boolean userAlreadyExisted = false;
		if (userAlreadyExisted) {
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
	}
	
	private void validatePassword() {
		if (Utility.isEmptyTextField(password)) {
			System.err.println("Passwords is empty!");
		}
		
		if (!password.getText().equals(confirmPassword.getText())) {
			System.err.println("Passwords not matching!");
		}
		
		boolean isPasswordWeak = false;
		if (isPasswordWeak) {
			System.err.println("Password should contains at least ... !");
		}
	}
	
	private void validateEmail() {
		if (Utility.isEmptyTextField(email)) {
			System.err.println("Email is empty!");
		}
		
		if (!Utility.isValidEmail(email.getText())) {
			System.err.println("Email address is invalid: " + email.getText());
		}
		
		boolean isEmailAlreadyExisted = true;
		if (isEmailAlreadyExisted) {
			// print warning message
		}
	}
	
	private void validateDoB() {
		if (dateOfBirth.getValue().equals("")) {
			System.err.println("Email is empty!");
		}
	}
}
