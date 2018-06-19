/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.adapter.po;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.util.ContentStateUtil;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.HashUtil;
import org.zanata.util.ShortString;

import com.google.common.collect.ImmutableSet;

public class PoReader2 {

    public static final ContentType PO_CONTENT_TYPE = new ContentType(
            "application/x-gettext");

    public static final ImmutableSet<String> POT_HEADER_FIELDS = ImmutableSet
            .of(HeaderFields.KEY_ProjectIdVersion,
                    HeaderFields.KEY_ReportMsgidBugsTo,
                    HeaderFields.KEY_PotCreationDate,
                    HeaderFields.KEY_MimeVersion, HeaderFields.KEY_ContentType,
                    HeaderFields.KEY_ContentTransferEncoding);

    public static final ImmutableSet<String> PO_HEADER_FIELDS = ImmutableSet
            .of(HeaderFields.KEY_PoRevisionDate,
                    HeaderFields.KEY_LastTranslator,
                    HeaderFields.KEY_LanguageTeam, HeaderFields.KEY_Language,
                    "Plural-Forms", "X-Generator");

    private boolean mapIdFromMsgctxt;

    public PoReader2(boolean mapIdFromMsgctxt) {
        this.mapIdFromMsgctxt = mapIdFromMsgctxt;
    }

    public PoReader2() {
        this(false);
    }

    /**
     * Extract contents of a PO file and convert to a TranslationsResource. NB:
     * If the file contains the gettext header Content-Type, it must be set to
     * ASCII, CHARSET, UTF8 or UTF-8, or an exception will occur.
     *
     * @param inputSource
     *            PO file to be extracted
     * @return converted PO file as TranslationsResource
     */
    public TranslationsResource extractTarget(InputSource inputSource) {
        TranslationsResource document = new TranslationsResource();
        MessageStreamParser messageParser = createParser(inputSource);

        boolean headerFound = false;
        while (messageParser.hasNext()) {
            Message message = messageParser.next();

            if (message.isHeader()) {
                if (headerFound)
                    throw new PoReaderException("Multiple headers found");
                headerFound = true;

                // add target header data
                PoTargetHeader poHeader = new PoTargetHeader();
                extractPoHeader(message, poHeader);
                document.getExtensions(true).add(poHeader);
            } else if (message.isObsolete()) {
                // TODO append obsolete
            } else {
                String id = createId(message);
                // add the target content (msgstr)
                TextFlowTarget tfTarget = new TextFlowTarget();
                tfTarget.setResId(id);
                List<String> sourceContents = getSourceContents(message);
                tfTarget.setSourceHash(HashUtil.sourceHash(sourceContents));
                tfTarget.setDescription(ShortString.shorten(message.getMsgid()));
                tfTarget.setContents(getContents(message));
                tfTarget.setState(getContentState(message));

                // add the PO comment
                tfTarget.getExtensions(true).add(
                        new SimpleComment(StringUtils.join(
                                message.getComments(), "\n")));
                document.getTextFlowTargets().add(tfTarget);
            }
        }
        return document;
    }

    /**
     * Checks that the file is safe to read as UTF-8.
     * @param hf
     */
    private static void checkContentType(HeaderFields hf) {
        String contentType = hf.getValue(HeaderFields.KEY_ContentType);
        if (contentType == null)
            return;
        String ct = contentType.toLowerCase();
        if (!ct.contains("charset="))
            return;
        if (ct.contains("charset=charset") || ct.contains("charset=ascii")
                || ct.contains("charset=utf-8") || ct.contains("charset=utf8")) {
            return;
        } else {
            throw new RuntimeException("unsupported charset in "
                    + HeaderFields.KEY_ContentType + ": " + contentType);
        }
    }

    private static void extractPotHeader(Message message, PoHeader potHeader) {
        potHeader.setComment(StringUtils.join(message.getComments(), "\n"));

        HeaderFields hf = HeaderFields.wrap(message);
        checkContentType(hf);
        for (String key : hf.getKeys()) {
            String val = hf.getValue(key);
            if (POT_HEADER_FIELDS.contains(key)) {
                potHeader.getEntries().add(new HeaderEntry(key, val));
            }
            // we add any custom fields to the PO only, not the POT
            // TODO this should be configurable
        }
    }

    private static void
            extractPoHeader(Message message, PoTargetHeader poHeader) {
        poHeader.setComment(StringUtils.join(message.getComments(), "\n"));

        HeaderFields hf = HeaderFields.wrap(message);
        checkContentType(hf);
        for (String key : hf.getKeys()) {
            String val = hf.getValue(key);
            if (PO_HEADER_FIELDS.contains(key)) {
                poHeader.getEntries().add(new HeaderEntry(key, val));
            } else if (!POT_HEADER_FIELDS.contains(key)) {
                // we add any custom fields to the PO only, not the POT
                // TODO this should be configurable
                poHeader.getEntries().add(new HeaderEntry(key, val));
            }
        }
    }

    /**
     * Extract contents of a POT file and convert to a Resource. NB: If the file
     * contains the gettext header Content-Type, it must be set to ASCII,
     * CHARSET, UTF8 or UTF-8, or an exception will occur.
     *
     * @param inputSource
     *            POT file to be extracted
     * @param sourceLocaleId
     *            locale of POT, used to set metadata fields
     * @param docName
     *            name of POT file (minus .pot extension) used to set metadata
     *            fields
     * @return converted POT file as Resource
     */
    public Resource extractTemplate(InputSource inputSource,
            LocaleId sourceLocaleId, String docName) {
        Resource document = new Resource(docName);
        MessageStreamParser messageParser = createParser(inputSource);

        document.setLang(sourceLocaleId);
        document.setContentType(PO_CONTENT_TYPE);
        List<TextFlow> resources = document.getTextFlows();

        boolean headerFound = false;
        while (messageParser.hasNext()) {
            Message message = messageParser.next();

            if (message.isHeader()) {
                if (headerFound)
                    throw new PoReaderException("Multiple headers found");
                headerFound = true;

                // store POT data
                PoHeader potHeader = new PoHeader();
                extractPotHeader(message, potHeader);
                document.getExtensions(true).add(potHeader);

            } else if (message.isObsolete()) {
                // TODO append obsolete
            } else {
                String id = createId(message);
                // add the content (msgid)
                TextFlow tf = new TextFlow(id, sourceLocaleId);
                tf.setPlural(message.isPlural());
                if (message.isPlural()) {
                    tf.setContents(message.getMsgid(), message.getMsgidPlural());
                } else {
                    tf.setContents(message.getMsgid());
                }
                resources.add(tf);

                // add the entry header POT fields
                tf.getExtensions(true).add(createPotEntryHeader(message));
                tf.getExtensions().add(createSimpleComment(message));
            }
        }
        return document;
    }

    private static PotEntryHeader createPotEntryHeader(Message message) {
        PotEntryHeader data = new PotEntryHeader();
        if (message.getMsgctxt() != null) {
            data.setContext(message.getMsgctxt());
        }
        data.getFlags().addAll(message.getFormats());
        data.getReferences().addAll(message.getSourceReferences());
        return data;
    }

    private static SimpleComment createSimpleComment(Message message) {
        String comment = StringUtils.join(message.getExtractedComments(), "\n");
        SimpleComment result = new SimpleComment(comment);
        return result;
    }

    static MessageStreamParser createParser(InputSource inputSource) {
        MessageStreamParser messageParser;
        if (inputSource.getCharacterStream() != null)
            messageParser =
                    new MessageStreamParser(inputSource.getCharacterStream());
        else if (inputSource.getByteStream() != null) {
            if (inputSource.getEncoding() != null)
                messageParser =
                        new MessageStreamParser(inputSource.getByteStream(),
                                Charset.forName(inputSource.getEncoding()));
            else
                messageParser =
                        new MessageStreamParser(inputSource.getByteStream(),
                                Charset.forName("UTF-8"));
        } else if (inputSource.getSystemId() != null) {
            try {
                URL url = new URL(inputSource.getSystemId());

                if (inputSource.getEncoding() != null)
                    messageParser =
                            new MessageStreamParser(url.openStream(),
                                    Charset.forName(inputSource.getEncoding()));
                else
                    messageParser =
                            new MessageStreamParser(url.openStream(),
                                    Charset.forName("UTF-8"));
            } catch (IOException e) {
                // TODO throw stronger typed exception
                throw new RuntimeException(
                        "failed to get input from url in inputSource", e);
            }
        } else
            // TODO throw stronger typed exception
            throw new RuntimeException("not a valid inputSource");

        return messageParser;
    }

    private List<String> getSourceContents(Message message) {
        List<String> sourceContents;
        if (message.isPlural()) {
            sourceContents = Arrays.asList(message.getMsgid(), message.getMsgidPlural());
        } else {
            sourceContents = Arrays.asList(message.getMsgid());
        }
        return sourceContents;
    }

    /**
     * Returns the contents of the Message (msgstr for singular, msgstr_plural
     * for plural) Also ensures at least one entry.
     *
     * @param message
     * @return
     */
    private static List<String> getContents(Message message) {
        if (message.isPlural()) {
            List<String> plurals = message.getMsgstrPlural();
            if (plurals.isEmpty()) {
                return Arrays.asList("");
            }
            return plurals;
        } else {
            return Arrays.asList(message.getMsgstr());
        }
    }

    // NB: we don't check that the number of msgstr_plurals matches nplurals on
    // the client, only on the server
    static ContentState getContentState(Message message) {
        ContentState requestedState =
                message.isFuzzy() ? ContentState.NeedReview
                        : ContentState.Translated;
        List<String> contents = getContents(message);
        return ContentStateUtil.determineState(requestedState, contents);
    }

    /**
     * Generate or extract id from message, using this reader's setting for
     * whether to extract the id from msgctxt.
     *
     * @see #createId(Message, boolean)
     */
    private String createId(Message message) {
        return createId(message, mapIdFromMsgctxt);
    }

    /**
     * Generate or extract id from a message.
     *
     * If id is extracted from msgctxt, the value of msgctxt is cleared and this
     * method will throw an exception on subsequent calls with the same message.
     *
     * @param message
     * @param mapIdFromMsgctxt
     *            true to extract id from msgctxt and set msgctxt to null,
     *            otherwise id is generated by hashing msgctxt and msgid.
     * @return extracted or generated id.
     *
     * @throws RuntimeException
     *             if called with mapIdFromMsgctxt=true but msgctxt is null or
     *             empty.
     */
    static String createId(Message message, boolean mapIdFromMsgctxt) {
        if (mapIdFromMsgctxt) {
            String zanataId = message.getMsgctxt();
            // null or empty id is not valid
            if (zanataId == null || zanataId.isEmpty()) {
                // TODO throw stronger typed exception
                throw new RuntimeException(
                        "Tried to map id from msgctxt but msgctxt was "
                                + zanataId + " for text flow with source: "
                                + message.getMsgstr());
            }
            // we do not want this msgctxt stored on the server
            message.setMsgctxt(null);
            return zanataId;
        }
        String sep = "\u0000";
        String hashBase =
                message.getMsgctxt() == null ? message.getMsgid() : message
                        .getMsgctxt() + sep + message.getMsgid();
        return HashUtil.generateHash(hashBase);
    }

}
