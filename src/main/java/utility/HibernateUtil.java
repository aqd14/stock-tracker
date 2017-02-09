package main.java.utility;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
        	String curWorkingDir = System.getProperty("user.dir");
        	File configFile = new File(curWorkingDir + File.separator + "src" + File.separator + "hibernate.cfg.xml");
			Configuration config = new Configuration().configure(configFile);
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
					.applySettings(config.getProperties());
        	sessionFactory = config.buildSessionFactory(builder.build());
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}