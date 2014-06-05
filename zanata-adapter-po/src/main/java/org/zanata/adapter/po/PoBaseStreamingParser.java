/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.adapter.po;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;
import org.zanata.adapter.StreamingParser;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.util.ContentStateUtil;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.util.HashUtil;
import org.zanata.util.ShortString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class PoBaseStreamingParser implements StreamingParser {

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

    protected boolean mapIdFromMsgctxt;

    public PoBaseStreamingParser() {
        this(false);
    }

    protected PoBaseStreamingParser(boolean mapIdFromMsgctxt) {
        this.mapIdFromMsgctxt = mapIdFromMsgctxt;
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

    static void
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

    static void extractPotHeader(Message message, PoHeader potHeader) {
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

    static List<String> getSourceContents(Message message) {
        List<String> sourceContents;
        if (message.isPlural()) {
            sourceContents = Arrays
                    .asList(message.getMsgid(), message.getMsgidPlural());
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
    static List<String> getContents(Message message) {
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

    static PotEntryHeader createPotEntryHeader(Message message) {
        PotEntryHeader data = new PotEntryHeader();
        if (message.getMsgctxt() != null) {
            data.setContext(message.getMsgctxt());
        }
        data.getFlags().addAll(message.getFormats());
        data.getReferences().addAll(message.getSourceReferences());
        return data;
    }

    static SimpleComment createSimpleComment(Message message) {
        String comment = StringUtils.join(message.getExtractedComments(), "\n");
        SimpleComment result = new SimpleComment(comment);
        return result;
    }

    /**
     * Checks that the file is safe to read as UTF-8.
     * 
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
}
