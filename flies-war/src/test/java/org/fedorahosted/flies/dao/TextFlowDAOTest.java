package org.fedorahosted.flies.dao;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups = { "jpa-tests" })
public class TextFlowDAOTest extends FliesDbunitJpaTest {

	private TextFlowDAO dao;
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
            new DataSetOperation(
            	"org/fedorahosted/flies/test/model/TextFlowTestData.dbunit.xml", 
            	DatabaseOperation.CLEAN_INSERT)
        );
    }

	@BeforeMethod(firstTimeOnly=true)
	public void setup() {
		dao = new TextFlowDAO((Session) getEm().getDelegate());
	}
	
	public void getIdsWithTranslations() {
		List<Long> de = dao.getIdsByTargetState(new LocaleId("de-DE"), ContentState.Approved);
		System.out.println(de);
		assertThat(de.size(), is(1));

		List<Long> es = dao.getIdsByTargetState(new LocaleId("es-ES"), ContentState.Approved);
		System.out.println(es);
		assertThat(es.size(), is(0));
		
		List<Long> fr = dao.getIdsByTargetState(new LocaleId("fr-FR"), ContentState.Approved);
		System.out.println(fr);
		assertThat(fr.size(), is(0));
	}

}
