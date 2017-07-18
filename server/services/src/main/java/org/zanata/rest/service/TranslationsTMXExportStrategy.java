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
package org.zanata.rest.service;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.XMLConstants;
import nu.xom.Attribute;
import nu.xom.Element;
import org.zanata.common.LocaleId;
import org.zanata.model.ITextFlow;
import org.zanata.model.ITextFlowTarget;
import org.zanata.util.TMXConstants;
import org.zanata.util.VersionUtility;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * Writes translations for Zanata Projects/TextFlows as TMX.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ParametersAreNonnullByDefault
public class TranslationsTMXExportStrategy
        implements TMXExportStrategy<ITextFlow> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationsTMXExportStrategy.class);

    private static class InvalidContentsException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidContentsException(String msg) {
            super(msg);
        }
    }

    private static final String creationTool =
            "Zanata " + TranslationsTMXExportStrategy.class.getSimpleName();
    private static final String creationToolVersion = VersionUtility
            .getVersionInfo(TranslationsTMXExportStrategy.class).getVersionNo();
    @Nullable
    private final LocaleId localeId;

    /**
     * Exports one or all locales.
     *
     * @param localeId
     *            locale to export, or null for all locales
     */
    public TranslationsTMXExportStrategy(@Nullable LocaleId localeId) {
        this.localeId = localeId;
    }

    @Override
    public Element buildHeader() throws IOException {
        Element header = new Element("header");
        header.addAttribute(new Attribute("creationtool", creationTool));
        header.addAttribute(
                new Attribute("creationtoolversion", creationToolVersion));
        header.addAttribute(new Attribute("segtype", "block"));
        header.addAttribute(new Attribute("o-tmf", "unknown"));
        header.addAttribute(new Attribute("adminlang", "en"));
        header.addAttribute(new Attribute("srclang", TMXConstants.ALL_LOCALE));
        header.addAttribute(new Attribute("datatype", "unknown"));
        return header;
    }

    public Optional<Element> buildTU(ITextFlow tf) throws IOException {
        try {
            Element tu = new Element("tu");
            setAttributes(tu, tf);
            for (Element tuv : buildTUVs(tf)) {
                tu.appendChild(tuv);
            }
            return Optional.of(tu);
        } catch (InvalidContentsException e) {
            log.warn(e.getMessage());
            return Optional.absent();
        }
    }

    private void setAttributes(Element tu, ITextFlow tf) {
        LocaleId sourceLocaleId = tf.getLocale();
        String tuid = tf.getQualifiedId();
        String srcLang = sourceLocaleId.getId();
        tu.addAttribute(new Attribute(TMXConstants.SRCLANG, srcLang));
        tu.addAttribute(new Attribute("tuid", tuid));
    }

    private Set<Element> buildTUVs(ITextFlow tf)
            throws InvalidContentsException {
        Set<Element> tuvSet = Sets.newLinkedHashSet();
        tuvSet.add(buildSourceTUV(tf));
        if (exportAllLocales()) {
            Iterable<ITextFlowTarget> allTargets = tf.getAllTargetContents();
            for (ITextFlowTarget target : allTargets) {
                Optional<Element> tuv = buildTargetTUV(target);
                tuvSet.addAll(tuv.asSet());
            }
        } else {
            ITextFlowTarget target = tf.getTargetContents(this.localeId);
            if (target != null) {
                Optional<Element> tuv = buildTargetTUV(target);
                tuvSet.addAll(tuv.asSet());
            }
        }
        return tuvSet;
    }

    private boolean exportAllLocales() {
        return this.localeId == null;
    }

    private Element buildSourceTUV(ITextFlow tf)
            throws InvalidContentsException {
        Element sourceTuv = new Element("tuv");
        sourceTuv.addAttribute(new Attribute("xml:lang",
                XMLConstants.XML_NS_URI, tf.getLocale().getId()));
        Element seg = new Element("seg");
        String srcContent = tf.getContents().get(0);
        if (srcContent.contains("\000")) {
            // this should be very rare, so we can afford to use an exception
            String msg =
                    "illegal null character; discarding SourceContents with id="
                            + tf.getQualifiedId();
            throw new InvalidContentsException(msg);
        }
        seg.appendChild(srcContent);
        sourceTuv.appendChild(seg);
        return sourceTuv;
    }

    private Optional<Element> buildTargetTUV(ITextFlowTarget target) {
        if (target.getState().isTranslated()) {
            LocaleId locId = target.getLocaleId();
            String trgContent = target.getContents().get(0);
            if (trgContent.contains("\000")) {
                String msg =
                        "illegal null character; discarding TargetContents with locale="
                                + locId + ", contents=" + trgContent;
                log.warn(msg);
                return Optional.absent();
            }
            Element tuv = new Element("tuv");
            tuv.addAttribute(new Attribute("xml:lang", XMLConstants.XML_NS_URI,
                    locId.getId()));
            Element seg = new Element("seg");
            seg.appendChild(trgContent);
            tuv.appendChild(seg);
            return Optional.of(tuv);
        }
        return Optional.absent();
    }
}
