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
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
abstract class PropertiesAbstractTest {

    String resourcePath = "src/test/resources/org/zanata/adapter/";
    GenericPropertiesAdapter adapter;

    Resource parseTestFile(String fileName) {
        File testFile = new File(resourcePath.concat(fileName));
        assert testFile.exists();
        return adapter.parseDocumentFile(testFile.toURI(), LocaleId.EN,
                Optional.absent());
    }

    TranslationsResource addTranslation(TranslationsResource resource,
                                        String id, String content, ContentState state) {
        TextFlowTarget textFlowTarget = new TextFlowTarget();
        textFlowTarget.setResId(id);
        textFlowTarget.setContents(content);
        textFlowTarget.setState(state);
        resource.getTextFlowTargets().add(textFlowTarget);
        return resource;
    }

    File createTempFile(Charset charset) throws Exception {
        File testFile = File.createTempFile("test-properties-temp-" + charset, ".properties");
        System.out.println(testFile);
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
