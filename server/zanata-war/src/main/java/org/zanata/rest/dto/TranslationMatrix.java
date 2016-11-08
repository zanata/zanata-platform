package org.zanata.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Data
@AllArgsConstructor
public class TranslationMatrix {
    private String savedDate;
    private String projectSlug;
    private String projectName;
    private String versionSlug;
    private LocaleId localeId;
    private String localeDisplayName;
    private ContentState savedState;
    private long wordCount;
}
