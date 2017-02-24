package main.java.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import main.java.model.Stock;
import main.java.utility.HibernateUtil;

/**
 * Home object for domain model class Stock.
 * @see .Stock
 * @author aqd14
 */
public class StockManager implements IManager {

	private static final Log log = LogFactory.getLog(StockManager.class);

	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	/**
	 * Add new stock to database
	 * @param stock
	 * @return False if user already existed or something went wrong with transaction
	 */
	@Override
	public void add(Object obj) {
		Stock stock = (Stock) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.save(stock);
		tx.commit();
	}
	
	@Override
	public void remove(Object obj) {
		// TODO Auto-generated method stub
		Stock stock = (Stock) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.delete(stock);
		tx.commit();
	}

	@Override
	public void update(Object obj) {
		Stock stock = (Stock) obj;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		session.update(stock);
		tx.commit();
	}
	
	public void persist(Stock transientInstance) {
		log.debug("persisting Stock instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(Stock instance) {
		log.debug("attaching dirty Stock instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Stock instance) {
		log.debug("attaching clean Stock instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(Stock persistentInstance) {
		log.debug("deleting Stock instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public Stock merge(Stock detachedInstance) {
		log.debug("merging Stock instance");
		try {
			Stock result = (Stock) sessionFactory.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public Stock findById(int id) {
		log.debug("getting Stock instance with id: " + id);
		try {
			Stock instance = (Stock) sessionFactory.getCurrentSession().get("Stock", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}
