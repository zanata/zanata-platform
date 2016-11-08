/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.DocNameWithExt;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.config.LocaleMapping;

import com.google.common.base.Optional;

/**
 * Strategy for uploading documents using the document file upload API methods.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPushStrategy extends AbstractCommonPushStrategy<PushOptions> {

    private static final Logger log = LoggerFactory
            .getLogger(RawPushStrategy.class);

    public static interface TranslationFilesVisitor {
        void visit(LocaleMapping locale, File translatedDoc);
    }

    /**
     * This implementation assumes that translated documents are named
     * identically to source documents, and reside in directories named with a
     * locale identifier that are siblings of the source directory.
     *
     * @param sourceDocument
     *            local path and name of source document for which to find
     *            translations
     * @param visitor
     */
    public void visitTranslationFiles(String sourceDocument,
            TranslationFilesVisitor visitor, Optional<String> translationExtension) {
        if (getOpts().getLocaleMapList() == null) {
            log.error("Locale mapping list not found, unable to push translations. Check your server settings.");
            return;
        }
        for (LocaleMapping localeMapping : getOpts().getLocaleMapList()) {
            File translationFile = new TransFileResolver(getOpts())
                    .resolveTransFile(DocNameWithExt.from(
                            sourceDocument), localeMapping, translationExtension);

            if (translationFile.canRead()) {
                visitor.visit(localeMapping, translationFile);
            } else {
                Object[] args = new Object[3];
                args[0] = localeMapping.getLocale();
                args[1] = localeMapping.getMapFrom();
                args[2] = translationFile.getAbsolutePath();
                log.warn(
                        "No translation file found for locale {} mapped by {}. Expected at {}",
                        args);
            }
        }

    }

}
