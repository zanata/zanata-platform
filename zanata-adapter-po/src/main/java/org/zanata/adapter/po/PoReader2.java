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

import java.io.InputStream;
import java.util.List;

import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PoReader2 {

    public static final ContentType PO_CONTENT_TYPE = new ContentType(
            "application/x-gettext");

    /**
     * Extract contents of a PO file and convert to a TranslationsResource. NB:
     * If the file contains the gettext header Content-Type, it must be set to
     * ASCII, CHARSET, UTF8 or UTF-8, or an exception will occur.
     *
     * @param inputStream
     *            PO file to be extracted
     * @return converted PO file as TranslationsResource
     */
    public TranslationsResource extractTarget(InputStream inputStream) {
        final TranslationsResource document = new TranslationsResource();

        PoTargetStreamingParser poParser = new PoTargetStreamingParser() {
            @Override
            public void handleTargetHeader(PoTargetHeader poHeader) {
                document.getExtensions(true).add(poHeader);
            }

            @Override
            public void handleTextFlowTarget(TextFlowTarget tft) {
                document.getTextFlowTargets().add(tft);
            }
        };
        try {
            poParser.parse(inputStream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    /**
     * Extract contents of a POT file and convert to a Resource. NB: If the file
     * contains the gettext header Content-Type, it must be set to ASCII,
     * CHARSET, UTF8 or UTF-8, or an exception will occur.
     *
     * @param inputStream
     *            POT file to be extracted
     * @param sourceLocaleId
     *            locale of POT, used to set metadata fields
     * @param docName
     *            name of POT file (minus .pot extension) used to set metadata
     *            fields
     * @return converted POT file as Resource
     */
    public Resource extractTemplate(InputStream inputStream,
            final LocaleId sourceLocaleId, String docName) {
        final Resource document = new Resource(docName);
        document.setLang(sourceLocaleId);
        document.setContentType(PO_CONTENT_TYPE);
        final List<TextFlow> resources = document.getTextFlows();

        PoSourceStreamingParser potParser = new PoSourceStreamingParser(sourceLocaleId) {
            @Override
            protected void handlePoHeader(PoHeader potHeader) {
                document.getExtensions(true).add(potHeader);
            }

            @Override
            protected void handleTextFlow(TextFlow textFlow) {
                resources.add(textFlow);
            }
        };
        try {
            potParser.parse(inputStream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return document;
    }
}
