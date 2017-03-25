/**
 * 
 */
package main.java.notification;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author doquocanh-macbook
 *
 */
public class EmailNotification implements INotification {
	private final String USER_NAME = "stock.tracker.cse.msu";  // GMail user name (just the part before "@gmail.com")
    private final String PASSWORD = "MSU39759"; // GMail password
    private final String SUBJECT = "[CSE-6214] Stock Tracker Notification";
    private final String GREETING = "Dear valued customer,\n\n";
    private final String SIGNATURE = "\n\n Best regards, \n\n Stock-Tracker team";

	/**
	 * 
	 */
	public EmailNotification() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see main.java.notification.INotification#notify(main.java.model.User)
	 */
	@Override
	public void notify(String sms, String to) {
		String toAddress[] = {to};
		StringBuilder bd = new StringBuilder(GREETING);
		bd.append(sms).append(SIGNATURE);
		sendNotification(USER_NAME, PASSWORD, toAddress, SUBJECT, bd.toString());
	}
	
	private void sendNotification(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

}
