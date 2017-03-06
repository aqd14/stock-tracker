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
}
