package org.zanata.adapter.glossary;

import com.google.common.base.Joiner;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryPoWriter extends AbstractGlossaryPullWriter {
    private static final Logger log =
        LoggerFactory.getLogger(GlossaryPoWriter.class);

    private final PoWriter poWriter;

    public GlossaryPoWriter(boolean encodeTabs) {
        this.poWriter = new PoWriter(encodeTabs);
    }

    public void write(final Writer writer, String charset,
        final List<GlossaryEntry> entries, final LocaleId srcLocale,
        final LocaleId targetLocale) throws IOException {
        if (entries == null || entries.isEmpty()) {
            log.warn("No glossary entries to process.");
            return;
        }
        try {
            for (GlossaryEntry entry : entries) {
                GlossaryTerm srcTerm =
                    getGlossaryTerm(entry.getGlossaryTerms(), srcLocale);
                GlossaryTerm transTerm =
                    getGlossaryTerm(entry.getGlossaryTerms(), targetLocale);

                if (srcTerm == null) {
                    continue;
                }
                Message message = new Message();
                message.setMsgid(srcTerm.getContent());
                message
                    .setMsgstr(transTerm == null ? "" : transTerm.getContent());
                String srcRef = entry.getSourceReference();
                String description = entry.getDescription();
                String comment = srcTerm.getComment();
                message.getSourceReferences().add(srcRef);
                message.getComments()
                    .add(Joiner.on("\n").join(description, comment));
                poWriter.write(message, writer);
                writer.write("\n");
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }
}
