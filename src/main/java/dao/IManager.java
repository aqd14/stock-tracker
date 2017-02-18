/**
 * 
 */
package main.java.dao;

/**
 * @author doquocanh-macbook
 *
 */
public interface IManager {
	/**
	 * Add new persistence object to database via <code>Hibernate</code>
	 * @param obj
	 */
	public void add(Object obj);
	
	public void remove(Object obj);
	
	public void update(Object obj);
}
