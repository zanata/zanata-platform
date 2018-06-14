package org.zanata.adapter.properties;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.openprops.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.PathUtil;

public class PropWriter {
    private static final Logger log = LoggerFactory.getLogger(PropWriter.class);

    public static enum CHARSET {
        UTF8(StandardCharsets.UTF_8),
        Latin1(StandardCharsets.ISO_8859_1);

        private final Charset alias;

        CHARSET(Charset alias) {
            this.alias = alias;
        }

        public Charset getAlias() {
            return alias;
        }
    }

    /**
     * Writes a properties file representation of the given {@link Resource} to
     * the given directory in {@link CHARSET#UTF8} or {@link CHARSET#Latin1} encoding.
     *
     * @param doc
     * @param baseDir
     * @param charset {@link CHARSET}
     * @throws IOException
     */
    public static void writeSource(final Resource doc, final File baseDir,
        final CHARSET charset) throws IOException {
        File baseFile = new File(baseDir, doc.getName() + ".properties");
        PathUtil.makeDirs(baseFile.getParentFile());

        log.debug("Creating base file {}", baseFile);
        Properties props = new Properties();
        for (TextFlow textFlow : doc.getTextFlows()) {
            List<String> contents = textFlow.getContents();
            if (contents.size() != 1) {
                throw new RuntimeException(
                    "file format does not support plural forms: resId="
                        + textFlow.getId());
            }
            props.setProperty(textFlow.getId(), textFlow.getContents().get(0));
            SimpleComment simpleComment =
                textFlow.getExtensions(true)
                    .findByType(SimpleComment.class);
            if (simpleComment != null && simpleComment.getValue() != null)
                props.setComment(textFlow.getId(), simpleComment.getValue());
        }
        // props.store(System.out, null);
        storeProps(props, baseFile, charset);
    }

    /**
     * Writes to given properties file of the given TranslationsResource
     * in {@link CHARSET#UTF8} or {@link CHARSET#Latin1} encoding.
     *
     * @param doc
     * @param propertiesFile
     * @param charset
     * @param createSkeletons
     * @throws IOException
     */
    public static void writeTranslationsFile(final Resource srcDoc,
            final TranslationsResource doc,
            final File propertiesFile, final CHARSET charset,
            boolean createSkeletons) throws IOException {

        Properties targetProp = new Properties();

        if (srcDoc == null) {
            for (TextFlowTarget target : doc.getTextFlowTargets()) {
                textFlowTargetToProperty(target.getResId(), target, targetProp,
                    createSkeletons);
            }
        } else {
            Map<String, TextFlowTarget> targets = new HashMap<>();
            if (doc != null) {
                for (TextFlowTarget target : doc.getTextFlowTargets()) {
                    targets.put(target.getResId(), target);
                }
            }
            for (TextFlow textFlow : srcDoc.getTextFlows()) {
                TextFlowTarget target = targets.get(textFlow.getId());
                textFlowTargetToProperty(textFlow.getId(), target, targetProp,
                    createSkeletons);
            }
        }
        storeProps(targetProp, propertiesFile, charset);
    }

    /**
     * Writes to given properties file of the given TranslationsResource
     * in {@link CHARSET#UTF8} or {@link CHARSET#Latin1} encoding.
     *
     * @param srcDoc
     * @param doc
     * @param baseDir
     * @param bundleName
     * @param locale
     * @param createSkeletons
     * @param charset
     *
     * @throws IOException
     */
    public static void writeTranslations(Resource srcDoc, final TranslationsResource doc,
        final File baseDir, String bundleName, String locale,
        final CHARSET charset, boolean createSkeletons) throws IOException {
        File langFile =
            new File(baseDir, bundleName + "_" + locale + ".properties");
        PathUtil.makeDirs(langFile.getParentFile());
        log.debug("Creating target file {}", langFile);

        writeTranslationsFile(srcDoc, doc, langFile, charset, createSkeletons);
    }

    private static void storeProps(Properties props, File file, CHARSET charset)
            throws IOException {
        BufferedOutputStream out =
                new BufferedOutputStream(new FileOutputStream(file));
        try {
            if (charset.alias.equals(StandardCharsets.UTF_8)) {
                Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8.displayName());
                props.store(writer, null);
            } else {
                props.store(out, null);
            }
        } finally {
            out.close();
        }
    }

    private static void textFlowTargetToProperty(String resId,
            TextFlowTarget target, Properties targetProp,
            boolean createSkeletons) {
        if (target == null || !target.getState().isTranslated()
                || target.getContents() == null
                || target.getContents().size() == 0) {
            // don't save fuzzy or empty values
            if (createSkeletons) {
                targetProp.setProperty(resId, "");
            }
            return;
        }
        List<String> contents = target.getContents();
        if (contents.size() != 1) {
            throw new RuntimeException(
                    "file format does not support plural forms: resId=" + resId);
        }
        targetProp.setProperty(target.getResId(), contents.get(0));
        SimpleComment simpleComment =
                target.getExtensions(true).findByType(SimpleComment.class);
        if (simpleComment != null && simpleComment.getValue() != null) {
            targetProp.setComment(target.getResId(), simpleComment.getValue());
        }
    }

}
