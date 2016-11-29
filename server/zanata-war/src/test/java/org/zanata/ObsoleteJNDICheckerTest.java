package org.zanata;

import javax.naming.Context;
import javax.naming.NamingException;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.service.MockInitialContextRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ObsoleteJNDICheckerTest {

    private Context context = Mockito.mock(Context.class);
    @Rule
    public MockInitialContextRule mockInitialContextRule = new MockInitialContextRule(context);
    private ObsoleteJNDIChecker obsoleteJNDIChecker;

    @Before
    public void setUp() {
        obsoleteJNDIChecker = new ObsoleteJNDIChecker();
    }

    @Test
    public void willDoNothingIfNoJNDIEntryExists() throws NamingException {
        when(context.lookup("java:global/zanata"))
                .thenThrow(new NamingException("not found"));
        obsoleteJNDIChecker.noObsoleteJNDIEntriesOrThrow();

        Assertions.assertThat(true).isEqualTo(true).describedAs("nothing blew up");
    }

    @Test(expected = IllegalStateException.class)
    public void willThrowExceptionIfObsoleteEntryExists()
            throws NamingException {
        when(context.lookup("java:global/zanata")).thenReturn(new Object());
        obsoleteJNDIChecker.noObsoleteJNDIEntriesOrThrow();
    }
}
