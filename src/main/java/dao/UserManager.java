package main.java.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import main.java.model.User;
import main.java.utility.HibernateUtil;

/**
 * Home object for domain model class User.
 * @see .User
 * @author aqd14
 */
public class UserManager {
	
	private static final Log log = LogFactory.getLog(UserManager.class);

	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	/**
	 * Add new user to database
	 * @param user
	 * @return False if user already existed or something went wrong with transaction
	 */
	public boolean create(User user) {
		// Check if user already existed in database by using email
//		User existingUser = findByEmail(user.getEmail());
//		if (null == existingUser) {
//			return false;
//		}
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(user);
		session.getTransaction().commit();
		return true;
	}
	
	public void persist(User transientInstance) {
		log.debug("persisting User instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(User instance) {
		log.debug("attaching dirty User instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(User instance) {
		log.debug("attaching clean User instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(User persistentInstance) {
		log.debug("deleting User instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public User merge(User detachedInstance) {
		log.debug("merging User instance");
		try {
			User result = (User) sessionFactory.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public User findById(int id) {
		log.debug("getting User instance with id: " + id);
		try {
			User instance = (User) sessionFactory.getCurrentSession().get("User", id);
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
	
	/**
	 * Find user in database with given email
	 * @param email
	 * @return NULL if not found
	 */
	public User findByEmail(String email) {
		log.debug("getting User instance with email: " + email);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchEmailHQL = "FROM User WHERE email = '" + email + "'";
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(searchEmailHQL);//.setParameter("email", email);
			List<User> users = query.getResultList();
			if (users == null || users.size() == 0) {
				log.debug("get successful, no instance found");
				return null;
			} else {
				log.debug("get successful, instance found");
			}
			return users.get(0);
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		} finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	public User findByUsernameOrEmail(String usernameOrEmail, String password) {
		log.debug("getting User instance with username or email: " + usernameOrEmail);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchUserHQL = "FROM User user WHERE user.username = '" + usernameOrEmail + "'" + " AND user.password  = '" + password
			        + "'" + "OR email = '" + usernameOrEmail + "'" + " AND user.password = '" + password + "'";
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(searchUserHQL);//.setParameter("email", email);
			List<User> users = query.getResultList();
			if (users == null || users.size() == 0) {
				log.debug("get successful, no instance found");
				return null;
			} else {
				log.debug("get successful, instance found");
			}
			return users.get(0);
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		} finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
