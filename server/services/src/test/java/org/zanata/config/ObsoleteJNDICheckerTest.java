package org.zanata.config;

import java.util.List;
import java.util.stream.Collectors;
import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.zanata.config.ObsoleteJNDIChecker;
import org.zanata.service.MockInitialContextRule;
import com.google.common.collect.Lists;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(Parameterized.class)
public class ObsoleteJNDICheckerTest {
    @Parameterized.Parameters(name= "{index}: checking jndi name [{0}]")
    public static Iterable<Object[]> data() {
        return ObsoleteJNDIChecker.OBSOLETE_ENTRIES.stream().map(entry -> new Object[] {entry}).collect(
                Collectors.toList());
    }

    private String obsoleteJNDIName;

    public ObsoleteJNDICheckerTest(String obsoleteJNDIName) {
        this.obsoleteJNDIName = obsoleteJNDIName;
    }

    private Context context = Mockito.mock(Context.class);
    private List<String> mockEntries;
    @Rule
    public MockInitialContextRule mockInitialContextRule = new MockInitialContextRule(context);

    @Before
    public void setUp() throws NamingException {
        mockEntries = Lists.newArrayList();
        // simulate how JNDI tree look up works
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String nameToLookUp = args[0].toString();
            boolean hasEntry = mockEntries.stream().anyMatch(entry -> entry.startsWith(nameToLookUp));
            if (hasEntry) {
                return new Object();
            } else {
                throw new NamingException("not found");
            }
        }).when(context).lookup(anyString());
    }

    @Test
    public void willDoNothingIfNoJNDIEntryExists() throws NamingException {
        ObsoleteJNDIChecker.ensureNoObsoleteJNDIEntries();
        // nothing blew up
    }

    @Test(expected = RuntimeException.class)
    public void willThrowExceptionIfObsoleteEntryExists()
            throws NamingException {
        mockEntries.add(obsoleteJNDIName);

        ObsoleteJNDIChecker.ensureNoObsoleteJNDIEntries();
    }
}
