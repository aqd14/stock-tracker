/**
 * 
 */
package main.java.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.ValidationRequest;
import com.twilio.type.PhoneNumber;

/**
 * @author doquocanh-macbook
 *
 */
public class PhoneNotification implements INotification {
	
	private static final String ACCOUNT_SID = "AC252e8acc74b966d2ff5d408cdea35981";
	private static final String AUTH_TOKEN = "36108bb859e6735abb943b8f6b7ca528";
	private static final String fromNumber = "+16625243364";
	
	public PhoneNotification() {
		
	}
	
	/**
	 * Register a new phone number to Twilio's caller ID
	 * @param phoneNumber
	 */
	public static void registerPhoneNumber(String phoneNumber) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		 ValidationRequest validationRequest = ValidationRequest.creator(new PhoneNumber(phoneNumber))
			        .setFriendlyName("My Home Phone Number")
			        .create();
		 validationRequest.getValidationCode();
	}
	
	/**
	 * Send message to a given phone number by using service provided by Twilio
	 * 
	 * @param sms Message content
	 * @param toNumber	To phone number	
	 */
	private static void sendMessage(String sms, String toNumber) {
	    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message.creator(new PhoneNumber(toNumber), new PhoneNumber(fromNumber), sms).create();
	}
	
	/* (non-Javadoc)
	 * @see main.java.INotification#notify(main.java.User)
	 */
	@Override
	public void notify(String sms, String phoneNumber) {
		sendMessage(sms, phoneNumber);
	}

}
