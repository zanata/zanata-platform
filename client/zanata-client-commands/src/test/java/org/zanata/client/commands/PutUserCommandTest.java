package org.zanata.client.commands;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;

import org.zanata.rest.client.*;
import org.zanata.rest.dto.Account;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import static org.zanata.client.commands.Messages.get;

public class PutUserCommandTest {

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    private final String username = "jcitizen";
    private final String key = "1234567890";

    @Mock
    private PutUserOptionsImpl opts;

    @Mock
    private AccountClient accountClient;

    @Mock
    private RestClientFactory restClientFactory;

    @Mock
    private PutUserCommand command;

    @Mock
	private Appender mockAppender;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);

        opts = new PutUserOptionsImpl();
        opts.setBatchMode(false);
        opts.setUsername("admin");
        opts.setKey("1234567890");
        opts.setUrl(new URL("http://localhost:8080/zanata"));

        when(restClientFactory.getAccountClient()).thenReturn(accountClient);
        Mockito.doNothing().when(restClientFactory).performVersionCheck();
    }

    @Test
    public void testValidUserAccountCreate() throws Exception {
        Account expected = new Account();
        expected.setUsername(username);
        opts.setUserUsername(username);

        expected.setApiKey(key);
        opts.setUserKey(key);

        expected.setEmail("email@example.com");
        opts.setUserEmail("email@example.com");

        expected.setName("James");
        opts.setUserName("James");

        expected.setEnabled(true);
        ConsoleInteractor consoleInteractor =
                MockConsoleInteractor.predefineAnswers("y");
        command = new PutUserCommand(opts, restClientFactory, consoleInteractor);
        command.run();
        verify(mockAppender, times(15))
                .doAppend(captorLoggingEvent.capture());
        assertTrue(wasLogged(get("no.passwordhash.set"), Level.WARN));
        Mockito.verify(restClientFactory, times(2)).getAccountClient();
        Mockito.verify(accountClient).put(username, expected);
    }

    @Test
    public void testValidUserAccountUpdate() throws Exception {
        Account existing = getGenericAccount();
        opts.setUserUsername(username);
        when(accountClient.get(username)).thenReturn(existing);

        existing.setEmail("jane@example.com");
        opts.setUserEmail("jane@example.com");

        existing.setName("James");
        opts.setUserName("Jane");

        existing.setEnabled(false);
        opts.setUserEnabled("false");

        existing.setRoles(new HashSet<>(Arrays.asList("user", "glossarist")));
        opts.setUserRoles("user,glossarist");

        existing.setTribes(new HashSet<>(Arrays.asList("en-AU", "ru")));
        opts.setUserLangs("en-AU,ru");

        ConsoleInteractor consoleInteractor =
                MockConsoleInteractor.predefineAnswers("y");
        command = new PutUserCommand(opts, restClientFactory, consoleInteractor);
        command.run();

        Mockito.verify(restClientFactory, times(2)).getAccountClient();
        Mockito.verify(accountClient).put(username, existing);
    }

/*    @Test
    public void testSetNewUserDisabled() {
        Account user = getGenericAccount();
        opts.setUserEmail(user.getEmail());
        opts.setUserName(user.getName());
        opts.setUserUsername(user.getUsername());

        opts.setUserEnabled("false");
        user.setEnabled(false);

        Mockito.verify(restClientFactory, times(2)).getAccountClient();
        Mockito.verify(accountClient).put(username, user);
    }*/

    @Test
    public void newUserNameAndEmailRequired() throws Exception {
        opts.setUserName(null);
        opts.setUserEmail("test@test.com");

        command = new PutUserCommand(opts, restClientFactory);
        expectException.expect(RuntimeException.class);
        expectException.expectMessage("New user's name and email must be specified");
        command.run();

        opts.setUserName("username");
        opts.setUserEmail(null);

        command = new PutUserCommand(opts, restClientFactory);
        expectException.expect(RuntimeException.class);
        expectException.expectMessage("New user's name and email must be specified");
        command.run();
    }

    private Account getGenericAccount() {
        Account existing = new Account(
                "jcitizen@example.com",
                "John Citizen",
                username,
                "UZMf4PIqtTBGAo9wWKuTpg==");
        existing.setEnabled(true);
        existing.setApiKey("0987654321234567890");
        existing.setRoles(new HashSet<>(Arrays.asList("user")));
        existing.setTribes(new HashSet<>(Arrays.asList("en-AU")));
        return existing;
    }

    private boolean wasLogged(String expected, Level level) {
        for (LoggingEvent loggingEvent :  captorLoggingEvent.getAllValues()) {
            if (loggingEvent.getMessage().equals(expected) &&
                    loggingEvent.getLevel().equals(level)) {
                return true;
            }
        }
        return false;
    }
}
