package main.java.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import main.java.common.CommonDefine;
import main.java.model.Transaction;
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
	public List<TransactionWrapper> findTransactions(Integer userId, int stockType) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql;
			switch(stockType) {
				case CommonDefine.OWNED_STOCK:
					hql = "SELECT transaction "
							+ "FROM Transaction transaction "
							+ "INNER JOIN Stock stock "
							+ "ON transaction.stockId = stock.id "
							+ "AND transaction.account.userId = :userId "
							+ "INNER JOIN UserStock us "
							+ "ON us.id.userId = :userId "
							+ "AND stock = us.stock "
							+ "AND (us.stockType = 1 "
							+ "OR us.stockType = 2) "
							+ "ORDER BY transaction.transactionDate DESC";
					break;
				case CommonDefine.TRANSACTION_STOCK:
//					hql = "from Transaction transaction WHERE transaction.account.userId = :userId ORDER BY transaction.transactionDate DESC";
					hql = "SELECT transaction "
							+ "FROM Transaction transaction "
							+ "INNER JOIN Stock stock "
							+ "ON transaction.stockId = stock.id "
							+ "AND transaction.account.userId = :userId "
							+ "INNER JOIN UserStock us "
							+ "ON us.id.userId = :userId "
							+ "AND stock = us.stock "
							+ "AND us.stockType != 2 " // Don't include remaining stock
							+ "ORDER BY transaction.transactionDate DESC";
					break;
				default:
					// Invalid parameter
					return null;
			}
			@SuppressWarnings("unchecked")
			Query<Transaction> query = session.createQuery(hql);
//			if (stockType == CommonDefine.OWNED_STOCK) {
			query.setParameter("userId", userId);
//			}
			List<Transaction> transactions = (List<Transaction>)query.getResultList();
			session.close();
			List<TransactionWrapper> wrapper = new ArrayList<>();
			for (Transaction t : transactions) {
				wrapper.add(new TransactionWrapper(t, t.getStock()));
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
