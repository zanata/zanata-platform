package org.zanata.adapter.glossary;

import org.apache.commons.lang3.StringUtils;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.google.common.base.Charsets;

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

    /**
     * @see {@link #write(Writer, List, LocaleId, LocaleId)}
     */
    public void write(@Nonnull OutputStream stream,
            @Nonnull final List<GlossaryEntry> entries,
            @Nonnull final LocaleId srcLocale,
            @Nonnull final LocaleId targetLocale) throws IOException {
        OutputStreamWriter osWriter =
                new OutputStreamWriter(stream, Charsets.UTF_8);
        write(osWriter, entries, srcLocale, targetLocale);
    }

    /**
     * This output a single po files of given <code>targetLocale</code>.
     * {@link GlossaryEntry#description} and comment from source term
     * {@link GlossaryEntry#srcLang} and translation term will be used
     * as comments.
     */
    public void write(@Nonnull final Writer fileWriter,
        @Nonnull final List<GlossaryEntry> entries,
        @Nonnull final LocaleId srcLocale,
        @Nonnull final LocaleId targetLocale) throws IOException {
        if (fileWriter == null) {
            log.warn("Missing fileWriter.");
            return;
        }
        if (entries == null) {
            log.warn("No glossary entries to process.");
            return;
        }
        if (srcLocale == null || targetLocale == null) {
            log.warn("Missing source locale and translation locale.");
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
                if (StringUtils.isNotBlank(srcRef)) {
                    message.getSourceReferences().add(srcRef);
                }
                addCommentIfNotEmpty(message, entry.getDescription());
                addCommentIfNotEmpty(message, srcTerm.getComment());
                addCommentIfNotEmpty(message,
                        transTerm == null ? null : transTerm.getComment());

                poWriter.write(message, fileWriter);
                fileWriter.write("\n");
            }
        } finally {
            fileWriter.flush();
        }
    }

    private static void addCommentIfNotEmpty(Message message, String comment) {
        if (StringUtils.isNotBlank(comment)) {
            message.addComment(comment);
        }
    }
}
