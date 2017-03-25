/**
 * 
 */
package main.java.notification;

/**
 * @author doquocanh-macbook
 *
 */

public interface INotification {
	/**
	 * Notify user when stock has update
	 * 
	 * @param sms	The content of notification
	 * @param identification	Communication channel, can be either email or phone
	 */
	public void notify(String sms, String identification);
}
