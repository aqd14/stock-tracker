/**
 * 
 */
package main.java.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextField;

/**
 * @author aqd14
 *
 */
public class Utility {
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	/**
	 * 
	 */
	public Utility() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Verify if the given text field is empty or not
	 * @param tf Given TextField instance
	 * @return True if not empty, otherwise returns False
	 */
	public static boolean isEmptyTextField(TextField tf) {
		if (tf == null) {
			return true;
		}
		return tf.getText().equals("");
	}
	
	/**
	 * Validate email address by using regular expression
	 * @param email
	 * @return True if email is valid, otherwise return False
	 */
	public static boolean isValidEmail(String email) {
	        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
	        return matcher.find();
	}
}
