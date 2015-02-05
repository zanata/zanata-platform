package org.zanata.rest.dto.matrix;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Data
@AllArgsConstructor
public class DetailMatrix {
    private String projectSlug;
    private String versionSlug;
    private LocaleId localeId;
    private ContentState savedState;
    private long wordCount;
}
