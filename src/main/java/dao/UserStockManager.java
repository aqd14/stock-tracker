package main.java.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import main.java.model.UserStock;
import main.java.utility.HibernateUtil;

public class UserStockManager<T> extends BaseManager<T> {
//	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

//	@Override
//	public void add(Object obj) {
//		UserStock manager = (UserStock) obj;
//		Session session = null;
//		try {
//			session = sessionFactory.getCurrentSession();
//			Transaction tx = session.beginTransaction();
//			session.save(manager);
//			tx.commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (session != null) {
//				session.close();
//			}
//		}
//	}
//	
//	@Override
//	public void remove(Object obj) {
//		UserStock manager = (UserStock) obj;
//		Session session = null;
//		try {
//			session = sessionFactory.getCurrentSession();
//			Transaction tx = session.beginTransaction();
//			session.delete(manager);
//			tx.commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (session != null) {
//				session.close();
//			}
//		}
//	}
//
//	@Override
//	public void update(Object obj) {
//		UserStock manager = (UserStock) obj;
//		Session session = null;
//		try {
//			session = sessionFactory.getCurrentSession();
//			Transaction tx = session.beginTransaction();
//			session.update(manager);
//			tx.commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (session != null) {
//				session.close();
//			}
//		}
//	}
	
//	/**
//	 * Find list of stock current use owns given stock code
//	 * @param userId
//	 * @param stockCode
//	 * @return
//	 */
//	public List<Stock> findStocks(Integer userId, String stockCode) {
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//		session.beginTransaction();
//		try {
//			String hql = "SELECT stock FROM Stock stock INNER JOIN UserStock us"
//					+ " ON stock.id = us.id.stockId AND us.id.userId = :userID AND stock.stockCode = :stockCode";
//			@SuppressWarnings("unchecked")
//			Query<Stock> query = session.createQuery(hql);
//			query.setParameter("userID", userId);
//			query.setParameter("stockCode", stockCode);
//			List<Stock> stocks = (List<Stock>)query.getResultList();
//			session.close();
//			return stocks;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		} finally {
//			session.close();
//		}
//	}
	
	/**
	 * Check if user currently owns or settings alert for certain stock
	 * @param userId	User ID
	 * @param stockCode Stock code that is checked
	 * @return <code>true</code> if users own that stock. Otherwise returns <code>false</code>
	 */
	public boolean hasStock(Integer userId, String stockCode) {
		List<UserStock> us = findUserStock(userId, stockCode);
		return (null != us && us.size() > 0);
	}
	
	/**
	 * <p>
	 * Find UserStock instance in database that matches user's id and stock's id.
	 * This own is used when user try to sell owned stock. So this function will return an UserStock instance
	 * in which user owns stock.
	 * </p>
	 * @param userId
	 * @param stockId
	 * @return
	 */
	public UserStock findUserStock(Integer userId, Integer stockId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql = "SELECT us FROM UserStock us "
					+ "INNER JOIN Stock stock "
					+ "ON us.id.userId = :userID "
					+ "AND us.id.stockId = :stockID "
					+ "AND stock.id = :stockID "
					+ "AND stock.transaction is not null";
			@SuppressWarnings("unchecked")
			Query<UserStock> query = session.createQuery(hql);
			query.setParameter("userID", userId);
			query.setParameter("stockID", stockId);
			List<UserStock> userStocks = query.getResultList();
			if (userStocks != null && userStocks.size() > 0)
				return userStocks.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
		return null;
	}
	
	/**
	 * <p>
	 * 
	 * Find UserStock instance in database that matches user'id and stock code.
	 * Avoid using stock's id because when user settings alert, 
	 * selected stock is not existed in database so it doesn't have <code>id</code>
	 * 
	 * @see {@link #findUserStock(Integer, Integer)} if looking for owned relationship.
	 * </p>
	 * @param userId
	 * @param stockCode
	 * @return List of UserStock instances in which there is relationship between user and stocks. It is not necessary to be owned relationship
	 */
	public List<UserStock> findUserStock(Integer userId, String stockCode) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql = "SELECT us FROM UserStock us "
						+ "INNER JOIN Stock stock "
						+ "ON us.id.userId = :userID "
						+ "AND us.id.stockId = stock.id "
						+ "AND stock.stockCode = :stockCode";
			@SuppressWarnings("unchecked")
			Query<UserStock> query = session.createQuery(hql);
			query.setParameter("userID", userId);
			query.setParameter("stockCode", stockCode);
			List<UserStock> userStocks = query.getResultList();
			return userStocks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Get all UserStock instances that have at least one of alert settings threshold set.
	 * There are cases where returned list includes both same owned stocks and public stocks,
	 * this method will only pick owned stock (if any) of same stock
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserStock> findWithAlertSettingsOn() {
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String hql = "SELECT us FROM UserStock us "
					+ "WHERE us.valueThreshold != -1 "
					+ "OR us.combinedValueThreshold != -1 "
					+ "OR us.netProfitThreshold != -1";
//					+ "GROUP BY us.stock.stockName"; // Only select one instance for each stock. 
													 //	TODO: Think about the way to get only owned stock (if any)
			Query<UserStock> query = session.createQuery(hql);
			List<UserStock> userStocks = query.getResultList();
			return userStocks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
}
