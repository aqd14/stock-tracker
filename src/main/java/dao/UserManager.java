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
public class UserManager<T> extends BaseManager<T> {
	
	private static final Log log = LogFactory.getLog(UserManager.class);

	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	/**
	 * Add new user to database
	 * @param user
	 * @return False if user already existed or something went wrong with transaction
	 */
/*	@Override
	public void add(Object obj) {
		User user = (User) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.save(user);
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
		// TODO Auto-generated method stub
		User user = (User) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.delete(user);
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
		// TODO Auto-generated method stub
		User user = (User) obj;
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Transaction tx = session.beginTransaction();
			session.update(user);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}*/
	
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
	
	/**
	 * Search database to find a specific user. Based on value of password, can search for login
	 * or search only for update or reset password
	 * @param usernameOrEmail Registered username or email 
	 * @param password	Password
	 * @return User instance if found, else return null
	 */
	public User findByUsernameOrEmail(String usernameOrEmail, String password) {
		// Pre-condition check
		if (usernameOrEmail == null) {
			System.err.println("Username invalid!");
		}
		log.debug("getting User instance with username or email: " + usernameOrEmail);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchUserHQL = "";
			// If given both username, email and password
			if (password != null) {
				searchUserHQL = "FROM User user WHERE user.username = '" + usernameOrEmail + "'" + " AND user.password  = '" + password
				+ "'" + "OR email = '" + usernameOrEmail + "'" + " AND user.password = '" + password + "'";
			} else { // Search by username or email only
				searchUserHQL = "FROM User user WHERE user.username = '" + usernameOrEmail + "'" + "OR email = '" + usernameOrEmail + "'";
			}
			        
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
