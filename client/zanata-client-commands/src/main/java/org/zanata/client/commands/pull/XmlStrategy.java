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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.zanata.client.config.LocaleMapping;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.PathUtil;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class XmlStrategy extends AbstractPullStrategy {
    private JAXBContext jaxbContext;
    private Marshaller marshaller;
    StringSet extensions = new StringSet("comment;gettext");

    protected XmlStrategy(PullOptions opts) {
        super(opts);
        try {
            jaxbContext =
                    JAXBContext.newInstance(Resource.class,
                            TranslationsResource.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean needsDocToWriteTrans() {
        return false;
    }

    private String docNameToFilename(String docName) {
        return docName + ".xml";
    }

    @Override
    public void writeSrcFile(Resource doc) throws IOException {
        try {
            String filename = docNameToFilename(doc.getName());
            File srcFile = new File(getOpts().getSrcDir(), filename);
            PathUtil.makeParents(srcFile);
            marshaller.marshal(doc, srcFile);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public FileDetails writeTransFile(Resource doc, String docName,
            LocaleMapping locale, TranslationsResource targetDoc)
            throws IOException {
        try {
            File transFile = getTransFileToWrite(docName, locale);
            PathUtil.makeParents(transFile);
            marshaller.marshal(targetDoc, transFile);
            return null;
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public StringSet getExtensions() {
        return extensions;
    }
}
