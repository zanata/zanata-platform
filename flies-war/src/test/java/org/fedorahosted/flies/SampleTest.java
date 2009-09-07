package org.fedorahosted.flies;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups={"unit-tests"})
public class SampleTest {

	public void makeSureTrueIsTrue(){
		assertThat(true, is(true));
	}
}
