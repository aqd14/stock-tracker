/**
 * 
 */
package main.java.dao;

/**
 * @author doquocanh-macbook
 *
 */
public interface IManager<T> {
	/**
	 * Add new persistence object to database via <code>Hibernate</code>
	 * @param obj
	 */
	public boolean add(T obj);
	
	public boolean remove(T obj);
	
	public boolean update(T obj);
}
