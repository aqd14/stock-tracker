package main.java.utility;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class CommunicationUtil {
	
	private static final String ACCOUNT_SID = "AC252e8acc74b966d2ff5d408cdea35981";
	private static final String AUTH_TOKEN = "36108bb859e6735abb943b8f6b7ca528";
	private static final String fromNumber = "+16625243364";
	
	private CommunicationUtil() {

	}
	
	/**
	 * Send message to a given phone number by using service provided by Twilio
	 * 
	 * @param sms Message content
	 * @param toNumber	To phone number	
	 */
	public static void sendMessage(String sms, String toNumber) {
	    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message.creator(new PhoneNumber(toNumber), new PhoneNumber(fromNumber), sms).create();
	}
}
