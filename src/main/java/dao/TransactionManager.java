package main.java.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import main.java.model.Stock;
import main.java.model.TransactionWrapper;
import main.java.utility.HibernateUtil;

/**
 * Home object for domain model class Transaction.
 * @see .Transaction
 * @author aqd14
 */
public class TransactionManager<T> extends BaseManager<T> {

	/**
	 * <p>
	 * Using inner join query to retrieve list of transactions in Portfolio and Transaction History page.
	 * If <code>sold</code> is set to <code>false</code>, only pull out the currently owned stocks.
	 * Otherwise, pull out all transaction history.
	 * </p>
	 * 
	 * @param userId Current user id
	 * @param owned Flag to decide which transaction should be pulled out
	 * @return List of bought stock that user didn't sell yet
	 */
	public List<TransactionWrapper> findTransactions(Integer userId, boolean owned) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
//			String hql = "SELECT stock, transaction FROM Stock stock INNER JOIN Transaction transaction INNER JOIN Account account"
//					+ " ON account.userId = :userID";
			String hql;
			if (owned) {
				hql = "SELECT stock, transaction "
						+ "FROM Transaction transaction "
						+ "INNER JOIN Stock stock "
						+ "ON stock.transaction = transaction "
						// + "INNER JOIN Account account " 
						+ "AND transaction.account.userId = :userId "
						+ "INNER JOIN UserStock us "
						+ "ON us.id.userId = :userId AND stock = us.stock";
			} else {
				hql = "SELECT stock, transaction "
						+ "FROM Transaction transaction "
						+ "INNER JOIN Stock stock "
						+ "ON stock.transaction = transaction "
						// + "INNER JOIN Account account " 
						+ "AND transaction.account.userId = :userId";
			}
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
