package org.zanata.client;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zanata.client.commands.NullAbortStrategy;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ClientToServerTest {
    private ZanataClient client;

    @Before
    public void before() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        client =
                new ZanataClient(new NullAbortStrategy(), new PrintWriter(out),
                        new PrintWriter(err));
    }

    @Test
    @Ignore("this will talk to live server")
    public void testDisableSSLCertOption() {
        String command = "stats";
        String url = "https://translate.engineering.redhat.com/";
        String project = "iok";
        String version = "6.4";
        client.processArgs(command, "--url", url, "--project", project,
                "--project-version", version, "--disable-ssl-cert");
    }

}
