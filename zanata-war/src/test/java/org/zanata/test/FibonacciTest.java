package org.zanata.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@UseParametersRunnerFactory(CdiUnitRunnerWithParameters.Factory.class)
public class FibonacciTest {

//    @Inject

    @Parameters(name = "{index}: fib[{0}]={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 } });
    }

    @Parameter(0)
    private int fInput;

    @Parameter(1)
    private int fExpected;

    @Test
    public void test() {
        assertEquals(fExpected, (fInput));
    }

}
