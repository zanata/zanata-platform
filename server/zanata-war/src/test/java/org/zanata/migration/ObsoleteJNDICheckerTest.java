package org.zanata.migration;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.service.MockInitialContextRule;

import static org.mockito.Mockito.when;

public class ObsoleteJNDICheckerTest {

    private Context context = Mockito.mock(Context.class);
    @Rule
    public MockInitialContextRule mockInitialContextRule = new MockInitialContextRule(context);

    @Test
    public void willDoNothingIfNoJNDIEntryExists() throws NamingException {
        when(context.lookup("java:global/zanata/security/auth-policy-names"))
                .thenThrow(new NamingException("not found"));
        ObsoleteJNDIChecker.ensureNoObsoleteJNDIEntries();
        // nothing blew up
    }

    @Test(expected = IllegalStateException.class)
    public void willThrowExceptionIfObsoleteEntryExists()
            throws NamingException {
        when(context.lookup("java:global/zanata/security/auth-policy-names")).thenReturn(new Object());
        ObsoleteJNDIChecker.ensureNoObsoleteJNDIEntries();
    }
}
