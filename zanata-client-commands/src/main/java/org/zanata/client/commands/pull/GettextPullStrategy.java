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

import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class GettextPullStrategy extends AbstractGettextPullStrategy {

    public GettextPullStrategy(PullOptions opts) {
        super(opts);
    }

    @Override
    public File
            getTransFileToWrite(String docName, LocaleMapping localeMapping) {
        File transFile = new TransFileResolver(getOpts()).getTransFile(
            TransFileResolver.UnqualifiedSrcDocName.from(docName),
            localeMapping);
        return transFile;
    }

    @Override
    public FileDetails writeTransFile(Resource doc, String docName,
            LocaleMapping locMapping, TranslationsResource targetDoc)
            throws IOException {
        File transFile = getTransFileToWrite(docName, locMapping);
        return getPoWriter().writePoToFile(transFile, doc, targetDoc);
    }

}
