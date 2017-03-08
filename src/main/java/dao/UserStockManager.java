package main.java.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import main.java.model.Stock;
import main.java.model.UserStock;
import main.java.utility.HibernateUtil;

public class UserStockManager implements IManager {
	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

	@Override
	public void add(Object obj) {
		UserStock manager = (UserStock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.save(manager);
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
		UserStock manager = (UserStock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.delete(manager);
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
	public void update(Object obj) {
		UserStock manager = (UserStock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.update(manager);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	/**
	 * Find list of stock current use owns given stock code
	 * @param userId
	 * @param stockCode
	 * @return
	 */
	public List<Stock> findStocks(Integer userId, String stockCode) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql = "SELECT stock FROM Stock stock INNER JOIN UserStock us"
					+ " ON stock.id = us.id.stockId AND us.id.userId = :userID AND stock.stockCode = :stockCode";
			@SuppressWarnings("unchecked")
			Query<Stock> query = session.createQuery(hql);
			query.setParameter("userID", userId);
			query.setParameter("stockCode", stockCode);
			List<Stock> stocks = (List<Stock>)query.getResultList();
			session.close();
			return stocks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Check if user currently owns certain stock
	 * @param userId	User ID
	 * @param stockCode Stock code that is checked
	 * @return <code>true</code> if users own that stock. Otherwise returns <code>false</code>
	 */
	public boolean hasStock(Integer userId, String stockCode) {
		List<Stock> stocks = findStocks(userId, stockCode);
		return (null != stocks && stocks.size() > 0);
	}
	
	/**
	 * Find UserStock instance in database that matches user's id and stock's id
	 * @param userId
	 * @param stockId
	 * @return
	 */
	public UserStock findUserStock(Integer userId, Integer stockId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql = "SELECT us FROM UserStock us WHERE"
					+ " us.id.userId = :userID AND us.id.stockId = :stockID";
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
	 * Find UserStock instance in database that matches user'id and stock code
	 * @param userId
	 * @param stockCode
	 * @return
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
			query.setParameter("stockID", stockCode);
			List<UserStock> userStocks = query.getResultList();
			return userStocks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	}
}
