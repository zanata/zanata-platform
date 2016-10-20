package org.zanata.client.commands.pull;

import java.io.File;
import java.io.IOException;

import org.zanata.client.config.LocaleMapping;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * Strategy for converting documents from Zanata to a local file type. Every
 * implementation must have a public constructor which accepts PullOptions.
 */
public interface PullStrategy {
    /**
     * Which extensions (eg gettext, comment) does this strategy need to fetch
     * from the server?
     *
     * @return
     */
    StringSet getExtensions();

    /**
     * Indicates if this strategy must work without access to source files. No
     * attempt should be made to read, write or push source documents for a
     * trans-only strategy.
     *
     * @return true if this strategy only allows interactions with translation
     *         files.
     */
    public boolean isTransOnly();

    /**
     * Does this strategy need the source document (Resource) when writing
     * translations?
     *
     * @return
     */
    boolean needsDocToWriteTrans();

    /**
     * Provides the file reference that will be used to write a Translation file
     * for a given Resource. Ideally, this method should be used by concrete
     * implementations of the strategy to write the file.
     *
     * @param docName
     *            may be null if needsDocToWriteTrans() returns false
     * @param localeMapping
     *            Locale mapping to use.
     * @return A File reference (might not exist physically) to which a
     *         Translation Resource will be written.
     */
    File getTransFileToWrite(String docName, LocaleMapping localeMapping);

    /**
     * @param docWithLocalName
     * @throws IOException
     */
    void writeSrcFile(Resource docWithLocalName) throws IOException;

    /**
     * pre: docWithLocalName.getName() must match docName if docWithLocalName is
     * not null
     *
     * @param docWithLocalName
     *            may be null if needsDocToWriteTrans() returns false
     * @param docName
     *            may be null if needsDocToWriteTrans() returns false
     * @param localeMapping
     * @param targetDoc
     * @return Details of the file that was written. May be null if the Strategy
     *         cannot provide details.
     * @throws IOException
     */
    FileDetails writeTransFile(Resource docWithLocalName, String docName,
            LocaleMapping localeMapping, TranslationsResource targetDoc)
            throws IOException;
}
