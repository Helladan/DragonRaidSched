package bot.presistance;

import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import bot.domain.Data;
import bot.domain.Info;

public class DataPersist {

	private static SessionFactory sessionFactory;

	public DataPersist() {
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception e) {
			StandardServiceRegistryBuilder.destroy(registry);
			e.printStackTrace();
		}
	}

	public static void save(Data data) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		try {
			for (Entry<String, Info> info : data.getInfos().entrySet()) {
				info.getValue().setId(info.getKey());
				session.saveOrUpdate(info.getValue());
			}
			data.setId(0L);
			session.saveOrUpdate(data);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	public static Data retrive() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Data data = new Data();
		try {
			Query<Data> query = session.createQuery("from Data");
			List<Data> list = query.list();
			System.out.println(list);
			if(!list.isEmpty()) {
				data = list.get(0);
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return data;
	}
}
