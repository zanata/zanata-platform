package org.zanata.client.commands.push;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.common.FileTypeName;
import org.zanata.rest.client.RestClientFactory;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void extractDocTypeWithExtensionTest() {
        String fileNameAndExtension = "properties[xml]";
        String type = command.extractFileTypeName(fileNameAndExtension).getName();
        assertThat(type).isEqualTo("PROPERTIES");
    }

    @Test
    public void extractDocTypeWithoutExtensionTest() {
        String fileNameAndExtension = "properties";
        String type = command.extractFileTypeName(fileNameAndExtension).getName();
        assertThat(type).isEqualTo("PROPERTIES");
    }

    @Test
    public void extractDocTypeOnlyExtensionTest() {
        String fileNameAndExtension = "[xml]";
        FileTypeName type = command.extractFileTypeName(fileNameAndExtension);
        assertThat(type).isNull();
    }

    @Test
    public void extractExtensionWithTypeTest() {
        String fileNameAndExtension = "properties[xml]";
        Set<String> extensions =
                command.extractExtensions(fileNameAndExtension).keySet();
        assertThat(extensions).contains("xml");
    }

    @Test
    public void extractExtensionWithTypeTest2() {
        String fileNameAndExtension = "properties[xml;html]";
        Set<String> extensions =
                command.extractExtensions(fileNameAndExtension).keySet();
        assertThat(extensions).containsExactlyInAnyOrder("xml", "html");
    }

    @Test
    public void extractExtensionWithoutTypeTest() {
        String fileNameAndExtension = "[xml]";
        Set<String> extensions =
                command.extractExtensions(fileNameAndExtension).keySet();
        assertThat(extensions).contains("xml");
    }

    @Test
    public void extractExtensionWithoutTypeTest2() {
        String fileNameAndExtension = "[xml;html]";
        Set<String> extensions =
                command.extractExtensions(fileNameAndExtension).keySet();
        assertThat(extensions).containsExactlyInAnyOrder("xml", "html");
    }

    @Test
    public void extractExtensionOnlyTypeTest() {
        String fileNameAndExtension = "properties";
        Set<String> extensions =
                command.extractExtensions(fileNameAndExtension).keySet();
        assertThat(extensions).isEmpty();
    }
}
