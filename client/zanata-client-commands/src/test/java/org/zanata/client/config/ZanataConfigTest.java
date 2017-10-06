package org.zanata.client.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

public class ZanataConfigTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    Marshaller marshaller = jc.createMarshaller();
    File zanataProjectXml;
    File zanataUserFile;

    public ZanataConfigTest() throws Exception {
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    // public void testGenerateSchema() throws Exception
    // {
    // GenerateSchema.generateSchemaToStdout(jc);
    // }

    @Before
    public void setUp() throws IOException {
        zanataProjectXml = new File(tempFolder.newFolder(),
                "zanata.xml");
        zanataUserFile = new File(tempFolder.newFolder(),
                "zanata.ini");
    }

    @Test
    public void testWriteReadProject() throws Exception {
        writeProject();
        readProject();
    }

    void writeProject() throws Exception {
        ZanataConfig config = new ZanataConfig();
        config.setUrl(new URL("http://example.com"));
        config.setProject("project");
        config.setProjectVersion("version");
        config.getLocales().add(new LocaleMapping("fr", "fr-FR"));
        config.getLocales().add(new LocaleMapping("  zh-CN  "));
        marshaller.marshal(config, zanataProjectXml);
    }

    void readProject() throws Exception {
        ZanataConfig config =
                (ZanataConfig) unmarshaller.unmarshal(zanataProjectXml);
        assertThat(config.getUrl()).isEqualTo(new URL("http://example.com"));
        assertThat(config.getProject()).isEqualTo("project");
        assertThat(config.getProjectVersion()).isEqualTo("version");
        LocaleList locales = config.getLocales();
        assertThat(locales).hasSize(2);
        assertThat(locales.get(0).getLocale()).isEqualTo("fr");
        assertThat(locales.get(0).getMapFrom()).isEqualTo("fr-FR");
        assertThat(locales.get(1).getLocale()).isEqualTo("zh-CN");
        assertThat(locales.get(1).getMapFrom()).isNull();
    }

    @Test
    public void testWriteReadUser() throws Exception {
        writeUser();
        readUser();
    }

    void writeUser() throws Exception {
        FileConfiguration config =
                new HierarchicalINIConfiguration(zanataUserFile);
        config.setProperty("zanata.url", new URL("http://zanata.example.com/"));
        config.setProperty("zanata.username", "admin");
        config.setProperty("zanata.key", "b6d7044e9ee3b2447c28fb7c50d86d98");
        config.setProperty("zanata.debug", false);
        config.setProperty("zanata.errors", true);

        config.save();
    }

    void readUser() throws Exception {
        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());
        config.addConfiguration(new HierarchicalINIConfiguration(zanataUserFile));
        assertThat(config.getString("zanata.username")).isEqualTo("admin");
        assertThat(config.getBoolean("zanata.debug")).isFalse();
        assertThat(config.getBoolean("zanata.errors")).isTrue();
    }

    @Test
    public void canReadAndWriteRules() throws Exception {
        ZanataConfig zanataConfig = new ZanataConfig();
        zanataConfig.setRules(Lists.newArrayList(new FileMappingRule("*.odt",
                "{filename}_{locale}.{extension}")));
        marshaller.marshal(zanataConfig, zanataProjectXml);

        ZanataConfig config =
                (ZanataConfig) unmarshaller.unmarshal(zanataProjectXml);
        assertThat(config.getRules()).hasSize(1);
        FileMappingRule rule = config.getRules().get(0);
        assertThat(rule.getPattern()).isEqualTo("*.odt");
        assertThat(rule.getRule()).isEqualTo("{filename}_{locale}.{extension}");
    }

}
