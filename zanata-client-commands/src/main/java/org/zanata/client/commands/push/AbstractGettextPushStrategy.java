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

import static org.apache.commons.io.FilenameUtils.removeExtension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public abstract class AbstractGettextPushStrategy extends AbstractPushStrategy {
    private PoReader2 poReader;

    public AbstractGettextPushStrategy() {
        super(new StringSet("comment;gettext"), ".pot");
    }

    public Set<String> findDocNames(File srcDir, ImmutableList<String> includes,
            ImmutableList<String> excludes, boolean useDefaultExclude,
            boolean caseSensitive, boolean excludeLocaleFilenames)
            throws IOException {
        Set<String> localDocNames = new HashSet<String>();

        // populate localDocNames by looking in pot directory, ignore
        // excludeLocale option
        String[] srcFiles =
                getSrcFiles(srcDir, includes, excludes, false,
                        useDefaultExclude, caseSensitive);

        for (String potName : srcFiles) {
            String docName = removeExtension(potName);
            localDocNames.add(docName);
        }
        return localDocNames;
    }

    abstract Collection<LocaleMapping> findLocales();

    abstract File getTransFile(LocaleMapping locale, String docName);

    @Override
    public Resource loadSrcDoc(File sourceDir, String docName)
            throws IOException {
        File srcFile = new File(sourceDir, docName + getFileExtension());
        BufferedInputStream bis =
                new BufferedInputStream(new FileInputStream(srcFile));
        try {
            InputSource potInputSource = new InputSource(bis);
            potInputSource.setEncoding("utf8");
            // load 'srcDoc' from pot/${docID}.pot
            return getPoReader().extractTemplate(potInputSource,
                    new LocaleId(getOpts().getSourceLang()), docName);
        } finally {
            bis.close();
        }
    }

    @Override
    public void visitTranslationResources(String docName, Resource srcDoc,
            TranslationResourcesVisitor callback) throws IOException {
        for (LocaleMapping locale : findLocales()) {
            File transFile = getTransFile(locale, docName);
            if (transFile.canRead()) {
                BufferedInputStream bis =
                        new BufferedInputStream(new FileInputStream(transFile));
                try {
                    InputSource inputSource = new InputSource(bis);
                    inputSource.setEncoding("utf8");
                    TranslationsResource targetDoc =
                            getPoReader().extractTarget(inputSource);
                    callback.visit(locale, targetDoc);
                } finally {
                    bis.close();
                }
            }
        }
    }

    protected PoReader2 getPoReader() {
        if (poReader == null) {
            poReader = new PoReader2();
        }
        return poReader;
    }

}
