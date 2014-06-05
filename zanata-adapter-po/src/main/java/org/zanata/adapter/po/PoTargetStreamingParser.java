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

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.util.HashUtil;
import org.zanata.util.ShortString;

/**
 * Parses a PO target file in a streaming fashion. Headers and Text Flow target
 * events are fired and handled by the callback methods which implementations
 * must provide.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class PoTargetStreamingParser extends PoBaseStreamingParser {

    public PoTargetStreamingParser() {
        this(false);
    }

    protected PoTargetStreamingParser(boolean mapIdFromMsgctxt) {
        super(mapIdFromMsgctxt);
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

                // add target header data
                PoTargetHeader poHeader = new PoTargetHeader();
                extractPoHeader(message, poHeader);
                handleTargetHeader(poHeader);
            } else if (message.isObsolete()) {
                // TODO append obsolete
            } else {
                String id = createId(message, mapIdFromMsgctxt); // TODO
                                                                 // mapIdFromMsgContext
                // add the target content (msgstr)
                TextFlowTarget tfTarget = new TextFlowTarget();
                tfTarget.setResId(id);
                List<String> sourceContents = getSourceContents(
                        message);
                tfTarget.setSourceHash(HashUtil.sourceHash(sourceContents));
                tfTarget.setDescription(ShortString.shorten(message.getMsgid()));
                tfTarget.setContents(getContents(message));
                tfTarget.setState(getContentState(message));

                // add the PO comment
                tfTarget.getExtensions(true).add(
                        new SimpleComment(StringUtils.join(
                                message.getComments(), "\n")));
                handleTextFlowTarget(tfTarget);
            }
        }
    }

    public abstract void handleTargetHeader(PoTargetHeader poHeader);

    public abstract void handleTextFlowTarget(TextFlowTarget tft);

}
