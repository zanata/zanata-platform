import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;


import org.fedorahosted.flies.repository.model.project.HProject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateTests {

	private static EntityManagerFactory emf;

	@BeforeClass
	public static void initialize(){
		emf = Persistence.createEntityManagerFactory("openl10n");
	}
	
	@AfterClass
	public static void shutDown(){
		emf.close();
	}

	@Test
	public void addSomeSampleData(){
		// First unit of work
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		HProject project = new HProject("id", "name",null);
		em.persist(project);

		em.flush();
		tx.commit();
		em.close();

		// Second unit of work
		EntityManager newEm = emf.createEntityManager();

		List<HProject> projects = newEm.createQuery(
				"from HProject p").getResultList();

		System.out.println(projects.size() + " message(s) found:");

		for (HProject p : projects) {
			System.out.println(p.getId());
		}

		newEm.close();

	}

	@Test
	public void addMoreSampleData(){
		addSomeSampleData();
	}
	
}