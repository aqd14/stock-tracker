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
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.save(manager);
		tx.commit();
	}
	
	@Override
	public void remove(Object obj) {
		// TODO Auto-generated method stub
		UserStock manager = (UserStock) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.delete(manager);
		tx.commit();
	}

	@Override
	public void update(Object obj) {
		UserStock manager = (UserStock) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.update(manager);
		tx.commit();
	}
	
	/**
	 * Using inner join query to retrieve list of bought stocks in Portfolio
	 * 
	 * @param userId Current user id
	 * @return List of bought stock that user didn't sell yet
	 */
	public List<Stock> findStocksByUserID(Integer userId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		String hql = "SELECT stock FROM Stock stock INNER JOIN UserStock us"
				+ " ON stock.id = us.id.stockId AND us.id.userId = :userID";
		@SuppressWarnings("unchecked")
		Query<Stock> query = session.createQuery(hql);
		query.setParameter("userID", userId);
		List<Stock> stocks = (List<Stock>)query.getResultList();
		session.close();
		return stocks;
	}
}
