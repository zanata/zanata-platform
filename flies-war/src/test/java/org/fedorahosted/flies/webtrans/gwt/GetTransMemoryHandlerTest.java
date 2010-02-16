package org.fedorahosted.flies.webtrans.gwt;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.fedorahosted.flies.webtrans.gwt.GetTransMemoryHandler.toLuceneQuery;
import static org.fedorahosted.flies.webtrans.gwt.GetTransMemoryHandler.toHQLQuery;

@Test(groups={"unit-tests"})
public class GetTransMemoryHandlerTest {

	public void testHQLQuery() {
		assertThat("%plaintext%", is(toHQLQuery("plaintext")));
		assertThat("%like special characters ~% ~_ ~~ plus \\%", 
			is(toHQLQuery(
				"LIKE special characters % _ ~ plus \\")));
	}
	
	public void testLuceneQuery(){
		assertThat("plaintext", is(toLuceneQuery("plaintext")));
		assertThat("lucene special characters \\+ \\- \\&& \\|| \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\ plus % _", 
			is(toLuceneQuery(
				"lucene special characters + - && || ! ( ) { } [ ] ^ \" ~ * ? : \\ plus % _")));
	}
}
