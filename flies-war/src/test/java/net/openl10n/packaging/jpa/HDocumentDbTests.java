package net.openl10n.packaging.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.openl10n.packaging.jpa.document.HDocument;
import net.openl10n.packaging.jpa.document.HDocumentTarget;
import net.openl10n.packaging.jpa.document.HResource;
import net.openl10n.packaging.jpa.document.HTextFlow;
import net.openl10n.packaging.jpa.document.HTextFlowTarget;
import net.openl10n.packaging.jpa.project.HProject;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.junit.Before;
import org.junit.Test;


public class HDocumentDbTests extends DbTest{

	Long projectId;
	Long docId;
	
	@Before
	public void setup(){
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		HProject p = new HProject("myproject", "my project", "a sample project");
		HDocument doc = new HDocument("/my/doc.x", new ContentType("text/plain") );
		p.getDocuments().add(doc);
		em.persist(p);
		projectId = p.getId();
		docId = doc.getId();
		
		tx.commit();
		em.close();
	}
	
	@Test
	public void traverseResources() {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		HDocument doc = em.find(HDocument.class, docId);
		HTextFlow tf = new HTextFlow();
		tf.setContent("hello world");
		tf.setResId("tf1");
		tf.setRevision(1l);
		doc.getResourceTree().add(tf);
		
		em.flush();
		em.refresh(doc);
		assertThat(tf.getId(), notNullValue());
		assertThat(tf.getDocument(), notNullValue());

		LocaleId nbLocale = new LocaleId("nb");
		HDocumentTarget docTarget = new HDocumentTarget(doc, nbLocale);
		em.persist(docTarget);
		
		HTextFlowTarget tft = new HTextFlowTarget(docTarget, tf);
		tft.setContent("hei verden");
		tft.setState(ContentState.Final);
		em.persist(tft);
		
		em.refresh(doc);

		assertThat(tf.getTargets().get(new LocaleId("nb")), is(tft));
		assertThat(doc.getResources().get("tf1"), is( (HResource) tf) );
		
		tx.commit();
		
		em.close();
	}
}
