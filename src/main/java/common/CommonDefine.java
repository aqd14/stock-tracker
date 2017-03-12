/**
 * 
 */
package main.java.common;

/**
 * @author aqd14
 *
 */
public class CommonDefine {
	public static final String EMPTY_FIELD_ERR = "You can't leave this empty.";
	public static final String INVALID_NAME_ERR = "Your name should be alphabetical.";
	public static final String INVALID_EMAIL_ERR = "Please use only letters (a-z), numbers, and periods.";
	public static final String PASSWORD_NOT_MATCHED_ERR = "These passwords don't match. Please try again!";
	public static final String PASSWORD_TOO_SHORT_ERR = "Password should include at least 8 characters.";
	public static final String INVALID_USERNAME_LENGTH_ERR = "Please use between 6 and 15 characters.";
	public static final String EMAIL_TAKEN_ERR = "This email is taken. Try another.";
	public static final String USER_NOT_EXIST = "Given username or email is not exist. Try another.";
	public static final String CURRENT_PASSWORD_INCORRECT = "Current password is incorrect. Try again.";
	public static final String INVALID_PHONE_NUMBER = "Invalid phone number. Please try again.";
	
	// Alert message
	public static final String REMOVE_STOCK_SMS = "You owned this stock. Do you really want to remove it?";
	public static final String SELL_STOCK_SMS = "Are you sure you really want to sell these stocks?";
	public static final String BUY_STOCK_SUCCESSFUL_SMS = "Your transaction is successful!";
	public static final String UPDATE_ALERT_SETTINGS_SMS = "You've updated alert settings successfully!";
	
	// Stage title
	public static final String HOME_TITLE = "Stock Tracker";
	public static final String LOGIN_TITLE = "Login";
	public static final String REGISTRATION_TITLE = "Registration";
	public static final String RESET_PASSWORD_TITLE = "Reset Password";
	public static final String USER_SETTINGS_TITLE = "Settings";
	public static final String STOCK_DETAILS_TITLE = "Stock Details";
	public static final String ALERT_SETTINGS_TITLE = "Alert Settings";
	public static final String PORTFOLIO_TITLE = "Portfolio and Transaction History";
	
	/**
	 * Interval for display stock data.
	 * @author doquocanh-macbook
	 *
	 */
	public enum Interval {
		ONE_WEEK,
		ONE_MONTH,
		THREE_MONTH,
		SIX_MONTH,
		ONE_YEAR
	}
	
	public static final int OWNED_STOCK = 1;
	public static final int NOT_OWNED_STOCK = 0;
}
