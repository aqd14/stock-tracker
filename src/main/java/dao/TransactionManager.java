package main.java.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import main.java.model.Stock;
import main.java.model.TransactionWrapper;
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
	
	/**
	 * Using inner join query to retrieve list of bought stocks in Portfolio
	 * 
	 * @param userId Current user id
	 * @return List of bought stock that user didn't sell yet
	 */
	public List<TransactionWrapper> findTransactions(Integer userId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
//			String hql = "SELECT stock, transaction FROM Stock stock INNER JOIN Transaction transaction INNER JOIN Account account"
//					+ " ON account.userId = :userID";
			String hql = "SELECT stock, transaction "
					+ "FROM Transaction transaction "
					+ "INNER JOIN Stock stock "
					+ "ON stock.transaction = transaction "
					// + "INNER JOIN Account account " 
					+ "AND transaction.account.userId = :userId";
			@SuppressWarnings("unchecked")
			Query<Object> query = session.createQuery(hql);
			query.setParameter("userId", userId);
//			String sql = "SELECT {s.*}, {t.*} FROM stock s INNER JOIN transaction t INNER JOIN account a "
//						+ "WHERE s.transaction_id = t.id AND t.account_id = a.id AND a.user_id = 1";
//			Query<Object> query = session.createNativeQuery(sql);
			List<Object> transactions = query.getResultList();
			session.close();
			List<TransactionWrapper> wrapper = new ArrayList<>();
			// Extract Stock and Transaction instances from multiple table query
			for (int i = 0; i < transactions.size(); i ++) {
				Object[] objArr = (Object[])transactions.get(i);
				Stock stock = null;
				main.java.model.Transaction transaction = null;
				// Check type casting
				if (objArr[0] instanceof Stock) {
					stock = (Stock) objArr[0];
				}
				if (objArr[1] instanceof main.java.model.Transaction) {
					transaction = (main.java.model.Transaction) objArr[1];
				}
				wrapper.add(new TransactionWrapper(transaction, stock));
			}
			return wrapper;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	}
}
