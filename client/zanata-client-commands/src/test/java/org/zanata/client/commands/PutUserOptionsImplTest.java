package org.zanata.client.commands;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.client.commands.Messages.get;

public class PutUserOptionsImplTest {

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PutUserOptionsImpl opts;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        opts = new PutUserOptionsImpl();
        opts.setUsername("jcitizen");
        opts.setKey("1234567890");
        opts.setUrl(new URL("http://localhost:8080/zanata/"));
    }

    @Test
    public void testValidEnabledOptions() throws Exception {
        opts.setUserEnabled("true");
        assertThat(opts.isUserEnabled()).isEqualTo("true");
        opts.setUserEnabled("false");
        assertThat(opts.isUserEnabled()).isEqualTo("false");
        opts.setUserEnabled("auto");
        assertThat(opts.isUserEnabled()).isEqualTo("auto");
        expectException.expect(RuntimeException.class);
        expectException.expectMessage("--user-enabled requires true or false (or auto)");
        opts.setUserEnabled("invalid");
    }

    @Test
    public void testUserKeyCannotBeBlank() {
        opts.setUserKey("   ");
        assertThat(opts.getUserKey()).isNull();
    }

    @Test
    public void testCommandDescription() {
        assertThat(opts.getCommandName()).isEqualTo("put-user");
        assertThat(opts.getCommandDescription()).isEqualTo(get("command.description.put-user"));
    }
}
