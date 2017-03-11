/**
 * 
 */
package main.java.control;

/**
 * @author doquocanh-macbook
 *
 */
public interface Subject {
	public void register(Observer o);
	public void remove(Observer o);
	public void notifyObservers();
}
