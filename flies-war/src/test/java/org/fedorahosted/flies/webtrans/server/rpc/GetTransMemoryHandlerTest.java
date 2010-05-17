package org.fedorahosted.flies.webtrans.server.rpc;

import org.apache.lucene.queryParser.QueryParser;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups={"unit-tests"})
public class GetTransMemoryHandlerTest {

	// list of special chars taken from
	// http://lucene.apache.org/java/2_4_1/queryparsersyntax.html#Escaping%20Special%20Characters

	public void testLuceneQuery(){
		assertThat(
			QueryParser.escape("plaintext"), 
			is("plaintext"));
		assertThat(
			QueryParser.escape("lucene special characters + - && || ! ( ) " +
					"{ } [ ] ^ \" ~ * ? : \\ plus % _"), 
			is("lucene special characters \\+ \\- \\&\\& \\|\\| \\! \\( \\) " +
					"\\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\ plus % _"));
	}

}
