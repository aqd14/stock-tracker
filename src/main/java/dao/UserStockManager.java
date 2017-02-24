package main.java.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
}
