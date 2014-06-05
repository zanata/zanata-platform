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

import java.io.InputStream;
import java.util.List;

import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;
import org.zanata.adapter.StreamingParser;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

/**
 * Parses a PO source (POT) file in a streaming fashion. Headers and Text Flow
 * events are fired and handled by the callback methods which implementations
 * must provide.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class PoSourceStreamingParser extends PoBaseStreamingParser {

    private LocaleId sourceLocaleId;

    public PoSourceStreamingParser(boolean mapIdFromMsgctxt,
            LocaleId sourceLocaleId) {
        super(mapIdFromMsgctxt);
        this.sourceLocaleId = sourceLocaleId;
    }

    public PoSourceStreamingParser(LocaleId sourceLocaleId) {
        this.sourceLocaleId = sourceLocaleId;
    }

    @Override
    public void parse(InputStream contents) throws Exception {
        InputSource inputSource = new InputSource(contents);
        MessageStreamParser messageParser = createParser(inputSource);

        boolean headerFound = false;
        while (messageParser.hasNext()) {
            Message message = messageParser.next();

            if (message.isHeader()) {
                if (headerFound)
                    throw new IllegalStateException("found a second header!");
                headerFound = true;

                // store POT data
                PoHeader potHeader = new PoHeader();
                extractPotHeader(message, potHeader);
                handlePoHeader(potHeader);

            } else if (message.isObsolete()) {
                // TODO append obsolete
            } else {
                String id = createId(message, mapIdFromMsgctxt);
                // add the content (msgid)
                TextFlow tf = new TextFlow(id, sourceLocaleId);
                tf.setPlural(message.isPlural());
                if (message.isPlural()) {
                    tf.setContents(message.getMsgid(), message.getMsgidPlural());
                } else {
                    tf.setContents(message.getMsgid());
                }
                // add the entry header POT fields
                tf.getExtensions(true).add(createPotEntryHeader(message));
                tf.getExtensions().add(createSimpleComment(message));
                handleTextFlow(tf);
            }
        }
    }

    protected abstract void handlePoHeader(PoHeader potHeader);

    protected abstract void handleTextFlow(TextFlow textFlow);

}
