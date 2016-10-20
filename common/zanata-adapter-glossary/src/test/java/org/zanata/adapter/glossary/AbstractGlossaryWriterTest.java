package org.zanata.adapter.glossary;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractGlossaryWriterTest {
    protected GlossaryEntry generateGlossaryEntry(LocaleId srcLocale, String pos,
        String description) {
        GlossaryEntry entry = new GlossaryEntry();
        entry.setPos(pos);
        entry.setDescription(description);
        entry.setSrcLang(srcLocale);
        return entry;
    }

    protected GlossaryTerm generateGlossaryTerm(String content, LocaleId localeId) {
        GlossaryTerm term = new GlossaryTerm();
        term.setContent(content);
        term.setLocale(localeId);
        return term;
    }
}
