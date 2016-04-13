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

package org.zanata.client.commands.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import org.apache.commons.io.FilenameUtils;
import org.zanata.adapter.properties.PropReader;
import org.zanata.adapter.properties.PropWriter;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.DocNameWithoutExt;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * NB: you must initialise this object with init() after setPushOptions()
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PropertiesStrategy extends AbstractPushStrategy {
    // "8859_1" is used in Properties.java...
//    private static final String ISO_8859_1 = "ISO-8859-1";

    private PropReader propReader;

    private final PropWriter.CHARSET charset;

    public PropertiesStrategy() {
        this(PropWriter.CHARSET.Latin1);
    }

    public PropertiesStrategy(PropWriter.CHARSET charset) {
        super(new StringSet("comment"), ".properties");
        this.charset = charset;
    }

    @Override
    public void init() {
        this.propReader =
                new PropReader(charset,
                        new LocaleId(getOpts().getSourceLang()),
                        ContentState.Approved);
    }

    @Override
    public Set<String> findDocNames(File srcDir, ImmutableList<String> includes,
            ImmutableList<String> excludes, boolean useDefaultExclude,
            boolean caseSensitive, boolean excludeLocaleFilenames)
            throws IOException {
        Set<String> localDocNames = new HashSet<String>();

        String[] files =
                getSrcFiles(srcDir, includes, excludes, excludeLocaleFilenames,
                        useDefaultExclude, caseSensitive);

        for (String relativeFilePath : files) {
            String baseName = FilenameUtils.removeExtension(relativeFilePath);
            localDocNames.add(baseName);
        }
        return localDocNames;
    }

    private Resource loadResource(String docName, File propFile)
            throws IOException, RuntimeException {
        Resource doc = new Resource(docName);
        // doc.setContentType(contentType);
        try (FileInputStream in = new FileInputStream(propFile)) {
            propReader.extractTemplate(doc, in);
        }
        return doc;
    }

    @Override
    public Resource loadSrcDoc(File sourceDir, String docName)
            throws IOException, RuntimeException {
        String filename = docNameToFilename(docName);
        File propFile = new File(sourceDir, filename);
        return loadResource(docName, propFile);
    }

    private TranslationsResource loadTranslationsResource(Resource srcDoc,
            File transFile) throws IOException, RuntimeException {
        TranslationsResource targetDoc = new TranslationsResource();
        try (FileInputStream in = new FileInputStream(transFile)) {
            propReader.extractTarget(targetDoc, in, srcDoc);
        }
        return targetDoc;
    }

    @Override
    public void visitTranslationResources(String docName, Resource srcDoc,
            TranslationResourcesVisitor callback) throws IOException,
            RuntimeException {
        for (LocaleMapping locale : getOpts().getLocaleMapList()) {
            File transFile = new TransFileResolver(getOpts()).getTransFile(
                    DocNameWithoutExt.from(docName),
                    locale);
            if (transFile.exists()) {
                TranslationsResource targetDoc =
                        loadTranslationsResource(srcDoc, transFile);
                callback.visit(locale, targetDoc);
            } else {
                // no translation found in 'locale' for current doc
            }
        }
    }
}
