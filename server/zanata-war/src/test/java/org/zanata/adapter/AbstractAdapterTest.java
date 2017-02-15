package org.zanata.adapter;

import com.google.common.base.Optional;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import java.io.File;

/**
 * Common variables and functions for adapter tests
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
abstract class AbstractAdapterTest {

    FileFormatAdapter adapter;
    private String resourcePath = "src/test/resources/org/zanata/adapter/";

    Resource parseTestFile(String fileName) {
        File testFile = new File(resourcePath.concat(fileName));
        assert testFile.exists();
        return adapter.parseDocumentFile(testFile.toURI(),
                LocaleId.EN, Optional.absent());
    }

    File getTestFile(String fileName) {
        return new File(resourcePath.concat(fileName));
    }

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
}
