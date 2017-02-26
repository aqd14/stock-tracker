package main.java.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import main.java.utility.HibernateUtil;

/**
 * Home object for domain model class Transaction.
 * @see .Transaction
 * @author aqd14
 */
public class TransactionManager implements IManager {

private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	/**
	 * Add new stock to database
	 * @param stock
	 * @return False if user already existed or something went wrong with transaction
	 */
	@Override
	public void add(Object obj) {
		main.java.model.Transaction trans = (main.java.model.Transaction) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.save(trans);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	@Override
	public void remove(Object obj) {
		// TODO Auto-generated method stub
		main.java.model.Transaction trans = (main.java.model.Transaction) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.delete(trans);
		tx.commit();
	}

	@Override
	public void update(Object obj) {
		main.java.model.Transaction trans = (main.java.model.Transaction) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.update(trans);
		tx.commit();
	}
}
