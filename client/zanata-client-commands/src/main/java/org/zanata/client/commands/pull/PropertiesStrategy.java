/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands.pull;

import java.io.File;
import java.io.IOException;

import org.zanata.adapter.properties.PropWriter;
import org.zanata.client.dto.LocaleMappedTranslatedDoc;
import org.zanata.common.LocaleId;
import org.zanata.common.dto.TranslatedDoc;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PropertiesStrategy extends AbstractPullStrategy {
    StringSet extensions = new StringSet("comment");

    protected PropertiesStrategy(PullOptions opts) {
        super(opts);
    }

    @Override
    public StringSet getExtensions() {
        return extensions;
    }

    @Override
    public boolean needsDocToWriteTrans() {
        return false;
    }

    @Override
    public void writeSrcFile(Resource doc) throws IOException {
        PropWriter.writeSource(doc, getOpts().getSrcDir(), PropWriter.CHARSET.Latin1);
    }

    @Override
    public FileDetails writeTransFile(String docName,
            LocaleMappedTranslatedDoc doc) throws IOException {
        boolean createSkeletons = getOpts().getCreateSkeletons();
        File transFileToWrite = getTransFileToWrite(docName, doc.getLocale());
        LocaleId locale = new LocaleId(doc.getLocale().getLocale());
        TranslatedDoc transDoc = createSkeletons ?
                new TranslatedDoc(doc.getSource(), doc.getTranslation(), locale) :
                new TranslatedDoc(null, doc.getTranslation(), locale);
        PropWriter.writeTranslationsFile(transDoc,
                transFileToWrite, PropWriter.CHARSET.Latin1,
                createSkeletons, getOpts().getApprovedOnly());
        return null;
    }

}
