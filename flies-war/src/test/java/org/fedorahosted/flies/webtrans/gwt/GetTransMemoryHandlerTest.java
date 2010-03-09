package org.fedorahosted.flies.webtrans.gwt;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.fedorahosted.flies.webtrans.server.GetTransMemoryHandler.toFuzzyLuceneQuery;
import static org.fedorahosted.flies.webtrans.server.GetTransMemoryHandler.toLuceneQuery;

@Test(groups={"unit-tests"})
public class GetTransMemoryHandlerTest {

	// list of special chars taken from
	// http://lucene.apache.org/java/2_4_1/queryparsersyntax.html#Escaping%20Special%20Characters

	public void testLuceneQuery(){
		assertThat(
			toLuceneQuery("plaintext"), 
			is("plaintext"));
		assertThat(
			toLuceneQuery("lucene special characters + - && || ! ( ) " +
					"{ } [ ] ^ \" ~ * ? : \\ plus % _"), 
			is("lucene special characters \\+ \\- \\&\\& \\|\\| \\! \\( \\) " +
					"\\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\ plus % _"));
	}
	
	public void testFuzzyLuceneQuery(){
		assertThat(
				toFuzzyLuceneQuery(" lucene special characters + - && || ! ( ) " +
				"{ } [ ] ^ \" ~ * ? : \\ plus % _ "), 
				is(" lucene~ special~ characters~ \\+~ \\-~ \\&\\&~ \\|\\|~ \\!~ \\(~ \\)~ " +
				"\\{~ \\}~ \\[~ \\]~ \\^~ \\\"~ \\~~ \\*~ \\?~ \\:~ \\\\~ plus~ %~ _~ "));
	}
}
