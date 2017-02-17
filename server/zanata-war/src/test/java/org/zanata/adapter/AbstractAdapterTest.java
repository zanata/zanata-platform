package org.zanata.adapter;

import com.google.common.base.Optional;
import org.fedorahosted.openprops.Properties;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common variables and functions for adapter tests
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
abstract class AbstractAdapterTest {

    FileFormatAdapter adapter;
    String resourcePath = "src/test/resources/org/zanata/adapter/";

    Resource parseTestFile(String fileName) {
        File testFile = new File(resourcePath.concat(fileName));
        assert testFile.exists();
        return adapter.parseDocumentFile(testFile.toURI(),
                LocaleId.EN, Optional.absent());
    }

    File getTestFile(String fileName) {
        return new File(resourcePath.concat(fileName));
    }

    /*
     * Add a translation to a TranslationsResource
     */
    void addTranslation(TranslationsResource tRes,
                                String resId,
                                String content,
                                ContentState state) {
        TextFlowTarget textFlowTarget = new TextFlowTarget();
        textFlowTarget.setResId(resId);
        textFlowTarget.setContents(content);
        textFlowTarget.setState(state);
        tRes.getTextFlowTargets().add(textFlowTarget);
    }

    /*
     * Add a translation to a Map based translations resource
     */
    void addTranslation(Map<String, TextFlowTarget> map,
                        String resId,
                        String content,
                        ContentState state) {
        TextFlowTarget textFlowTarget = new TextFlowTarget();
        textFlowTarget.setContents(content);
        textFlowTarget.setState(state);
        map.put(resId, textFlowTarget);
    }

    /*
     * Create a .properties file in temp using the given character set
     */
    File createTempPropertiesFile(Charset charset) throws Exception {
        File testFile = File.createTempFile("test-properties-temp-" + charset, ".properties");
        assertThat(testFile.exists());
        Map<String, String> entries = new LinkedHashMap<>();
        if (charset == StandardCharsets.ISO_8859_1) {
            entries.put("line1", "ÀLine One");
            entries.put("line2", "ÀLine Two");
            entries.put("line3", "ÀLine Three");
        } else if (charset == StandardCharsets.UTF_8) {
            entries.put("line1", "¥Line One");
            entries.put("line2", "¥Line Two");
            entries.put("line3", "¥Line Three");
        }
        Properties resource = new Properties();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            resource.setProperty(entry.getKey(), entry.getValue());
        }
        resource.store(new OutputStreamWriter(new FileOutputStream(testFile),
                charset), null);
        return testFile;
    }
}
