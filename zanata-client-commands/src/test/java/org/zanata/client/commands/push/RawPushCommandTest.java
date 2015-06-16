package org.zanata.client.commands.push;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.rest.client.RestClientFactory;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RawPushCommandTest {

    @Mock
    private PushOptions opts;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private ConsoleInteractor console;

    private RawPushCommand command;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(opts.getProj()).thenReturn("project");
        when(opts.getProjectVersion()).thenReturn("version");
        when(opts.getProjectType()).thenReturn("File");
        command = new RawPushCommand(opts, clientFactory, console);
    }

    @Test
    public void extractFileTypeWithExtensionTest() {
        String fileNameAndExtension = "properties[xml]";
        String type = command.extractType(fileNameAndExtension);
        assertThat(type, equalTo("properties"));
    }

    @Test
    public void extractFileTypeWithoutExtensionTest() {
        String fileNameAndExtension = "properties";
        String type = command.extractType(fileNameAndExtension);
        assertThat(type, equalTo("properties"));
    }

    @Test
    public void extractFileTypeOnlyExtensionTest() {
        String fileNameAndExtension = "[xml]";
        String type = command.extractType(fileNameAndExtension);
        assertThat(type, equalTo(""));
    }

    @Test
    public void extractExtensionWithTypeTest() {
        String fileNameAndExtension = "properties[xml]";
        List<String> extensions = command.extractExtensions(fileNameAndExtension);
        assertThat(extensions, contains("xml"));
    }

    @Test
    public void extractExtensionWithTypeTest2() {
        String fileNameAndExtension = "properties[xml;html]";
        List<String> extensions = command.extractExtensions(fileNameAndExtension);
        assertThat(extensions, containsInAnyOrder("xml", "html"));
    }

    @Test
    public void extractExtensionWithoutTypeTest() {
        String fileNameAndExtension = "[xml]";
        List<String> extensions = command.extractExtensions(fileNameAndExtension);
        assertThat(extensions, contains("xml"));
    }

    @Test
    public void extractExtensionWithoutTypeTest2() {
        String fileNameAndExtension = "[xml;html]";
        List<String> extensions = command.extractExtensions(fileNameAndExtension);
        assertThat(extensions, containsInAnyOrder("xml", "html"));
    }

    @Test
    public void extractExtensionOnlyTypeTest() {
        String fileNameAndExtension = "properties";
        List<String> extensions = command.extractExtensions(fileNameAndExtension);
        assertThat(extensions.size(), equalTo(0));
    }
}
