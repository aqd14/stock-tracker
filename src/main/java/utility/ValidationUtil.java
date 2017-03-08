package main.java.utility;

import java.time.LocalDate;

import com.jfoenix.controls.JFXDatePicker;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import main.java.common.CommonMessage;
import main.java.dao.UserManager;
import main.java.model.User;

public class ValidationUtil {
	
	static UserManager userManager = new UserManager();
	
	public ValidationUtil() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Check if given text contains only numbers
	 * @param txt
	 * @return
	 */
	public static boolean containOnlyNumber(String txt) {
		return txt.matches("^[0-9]+$");
	}
	
	/**
	 * Validate user's first name.
	 * It shouldn't be empty. It shouldn't have special characters (!,@,#,$,%,&,*..)
	 */
	public static boolean validateFirstName(TextField firstNameTF, Text firstNameError) {
		// Validate first name's empty
		if (Utility.isTextFieldEmpty(firstNameTF)) {
			displayErrorMessage(firstNameError, CommonMessage.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!Utility.isNameValid(firstNameTF.getText())) {
			displayErrorMessage(firstNameError, CommonMessage.INVALID_NAME_ERR);
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
	public static boolean validateLastName(TextField lastNameTF, Text lastNameError) {
		// Validate first name
		if (Utility.isTextFieldEmpty(lastNameTF)) {
			displayErrorMessage(lastNameError, CommonMessage.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!Utility.isNameValid(lastNameTF.getText())) {
			displayErrorMessage(lastNameError, CommonMessage.INVALID_NAME_ERR);
			return false;
		}
		
		// Passed all phases, set error text invisible
		hideErrorMessage(lastNameError);
		return true;
	}
	
	public static boolean validateUsername(TextField usernameTF, Text usernameError) {
		// Validate first name
		if (Utility.isTextFieldEmpty(usernameTF)) {
			displayErrorMessage(usernameError, CommonMessage.EMPTY_FIELD_ERR);
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
			displayErrorMessage(usernameError, CommonMessage.INVALID_USERNAME_LENGTH_ERR);
			return false;
		}
		hideErrorMessage(usernameError);
		return true;
	}
	
	/**
	 * Validate current password when user changes password in [Settings]
	 * 
	 * @param passwordPF
	 * @param passwordError
	 * @return
	 */
	public static boolean validateCurrentPassword(String curPw, PasswordField passwordPF, Text passwordError) {
		if (!curPw.equals(passwordPF.getText())) {
			passwordError.setText(CommonMessage.CURRENT_PASSWORD_INCORRECT);
			passwordError.setVisible(true);
			return false;
		}
		hideErrorMessage(passwordError);
		return true;
	}
	
	/**
	 * Validate password when user registers new account, resets or changes password
	 * @param passwordPF
	 * @param passwordError
	 * @return
	 */
	public static boolean validateOriginalPassword(PasswordField passwordPF, Text passwordError) {
		// Validate empty
		if (Utility.isTextFieldEmpty(passwordPF)) {
			passwordError.setText(CommonMessage.EMPTY_FIELD_ERR);
			passwordError.setVisible(true);
			return false;
		}
		
		final int minLength = 8;
		if(passwordPF.getText().length() < minLength) {
			displayErrorMessage(passwordError, CommonMessage.PASSWORD_TOO_SHORT_ERR);
			return false;
		}
		// Passed all validation
		hideErrorMessage(passwordError);
		return true;
	}
	
	/**
	 * Validate if confirm password matches entered password
	 * @param passwordPF
	 * @param confirmPasswordPF
	 * @param confirmPasswordError
	 * @return
	 */
	public static boolean validateConfirmedPassword(PasswordField passwordPF, PasswordField confirmPasswordPF, Text confirmPasswordError) {
		if (Utility.isTextFieldEmpty(confirmPasswordPF)) {
			displayErrorMessage(confirmPasswordError, CommonMessage.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!passwordPF.getText().equals(confirmPasswordPF.getText())) {
			System.err.println("Passwords not matching!");
			displayErrorMessage(confirmPasswordError, CommonMessage.PASSWORD_NOT_MATCHED_ERR);
			// Reset password fields to empty
			passwordPF.setText("");
			confirmPasswordPF.setText("");
			return false;
		}
		// Passed all validation
		hideErrorMessage(confirmPasswordError);
		return true;
	}
	
	/**
	 * Validate email address:
	 * <p><ul>
	 * 	<li> Is email empty?
	 *  <li> Is email valid (contains only alphabets, underscore, and period)
	 *  <li> Is email already existing on database?
	 *  <ul><p>
	 */
	public static boolean validateEmail(TextField emailTF, Text emailError) {
		if (Utility.isTextFieldEmpty(emailTF)) {
			displayErrorMessage(emailError, CommonMessage.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!Utility.isValidEmail(emailTF.getText())) {
			displayErrorMessage(emailError, CommonMessage.INVALID_EMAIL_ERR);
			return false;
		}
		
		User user = userManager.findByEmail(emailTF.getText());
		if (null != user) {
			displayErrorMessage(emailError, CommonMessage.EMAIL_TAKEN_ERR);
			return false;
		}
		// Passed all validations
		hideErrorMessage(emailError);
		return true;
	}
	
	public static boolean validateDoB(JFXDatePicker dateOfBirthDP, Text dobError) {
		LocalDate dob = dateOfBirthDP.getValue();
		if (null == dob) {
			displayErrorMessage(dobError, CommonMessage.EMPTY_FIELD_ERR);
			return false;
		}
		LocalDate maxDate = LocalDate.of(2000, 1, 1);
		LocalDate minDate = LocalDate.of(1950, 1, 1);
		if (dob.isAfter(maxDate)) {
			displayErrorMessage(dobError, "You are too young to play stock!");
			return false;
		}
		
		if (dob.isBefore(minDate)) {
			displayErrorMessage(dobError, "You are too old to play stock!");
			return false;
		}
		
		hideErrorMessage(dobError);
		return true;
	}
	
	public static void displayErrorMessage(Text instance, String errMessage) {
		instance.setText(errMessage);
		instance.setVisible(true);
	}
	
	public static void hideErrorMessage(Text instance) {
		instance.setVisible(false);   
	}
}
