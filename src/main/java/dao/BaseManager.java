package main.java.dao;

import main.java.utility.HibernateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class BaseManager<T> implements IManager<T> {
	
	protected static final Log log = LogFactory.getLog(StockManager.class);

	protected final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	public BaseManager() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean add(T obj) {
//		Stock stock = (Stock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.save(obj);
			tx.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@Override
	public boolean remove(T obj) {
		// TODO Auto-generated method stub
//		Stock stock = (Stock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.delete(obj);
			tx.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@Override
	public boolean update(T obj) {
//		Stock stock = (Stock) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.update(obj);
			tx.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

}
