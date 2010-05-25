package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DocumentUtilsTest {

	private static DocumentUtils documentUtils = new DocumentUtils();
	
	@Test
	public void mergeNoTextFlows() {
		List<SourceTextFlow> from = new ArrayList<SourceTextFlow>();
		HDocument to = new HDocument();
		boolean changed = documentUtils.mergeTextFlows(from, to);
		
		assertThat(changed, is(false));
	}
	
	@Test
	public void mergeTextFlowWithOneFromChange() {
		List<SourceTextFlow> from = new ArrayList<SourceTextFlow>();

		SourceTextFlow tf1 = new SourceTextFlow("id", LocaleId.EN, "text1");
		from.add(tf1);
		
		HDocument to = new HDocument();
		boolean changed = documentUtils.mergeTextFlows(from, to);
		
		assertThat(changed, is(true));
	}
	
}
