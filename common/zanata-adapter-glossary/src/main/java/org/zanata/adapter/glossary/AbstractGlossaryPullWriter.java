package org.zanata.adapter.glossary;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryTerm;

import java.util.List;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractGlossaryPullWriter {
    protected GlossaryTerm getGlossaryTerm(List<GlossaryTerm> terms,
        LocaleId localeId) {
        for (GlossaryTerm term : terms) {
            if (term.getLocale().equals(localeId)) {
                return term;
            }
        }
        return null;
    }
}
