package org.zanata.client.commands.push;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.DocNameWithoutExt;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public class XliffStrategy extends AbstractPushStrategy {
    XliffReader reader = new XliffReader();
    Set<String> sourceFiles;

    public XliffStrategy() {
        super(new StringSet("comment"), ".xml");
    }

    @VisibleForTesting
    protected XliffStrategy(XliffReader reader) {
        super(new StringSet("comment"), ".xml");
        this.reader = reader;
    }

    @Override
    public Set<String> findDocNames(File srcDir, ImmutableList<String> includes,
            ImmutableList<String> excludes, boolean useDefaultExclude,
            boolean caseSensitive, boolean excludeLocaleFilenames)
            throws IOException {
        sourceFiles = new HashSet<String>();
        Set<String> localDocNames = new HashSet<String>();

        String[] files =
                getSrcFiles(srcDir, includes, excludes, excludeLocaleFilenames,
                        useDefaultExclude, caseSensitive);

        for (String relativeFilePath : files) {
            sourceFiles.add(relativeFilePath);
            String baseName = FilenameUtils.removeExtension(relativeFilePath);
            baseName = trimLocaleFromFile(baseName);
            localDocNames.add(baseName);
        }
        return localDocNames;
    }

    private String trimLocaleFromFile(String fileName) {
        if (fileName.contains("_")) {
            String loc = new LocaleId(getOpts().getSourceLang()).toJavaName();
            if (StringUtils.containsIgnoreCase(fileName, "_" + loc)) {
                fileName = fileName.replaceAll("_" + loc, "");
            }
        }
        return fileName;
    }

    @Override
    public Resource loadSrcDoc(File sourceDir, String docName)
            throws FileNotFoundException {
        File srcFile = null;
        for (String file : sourceFiles) {
            if (file.startsWith(docName) && file.endsWith(getFileExtension())) {
                srcFile = new File(sourceDir, file);
                break;
            }
        }
        return reader.extractTemplate(srcFile, new LocaleId(getOpts()
                .getSourceLang()), docName, getOpts().getValidate());
    }

    @Override
    public void visitTranslationResources(String docName, Resource srcDoc,
            TranslationResourcesVisitor visitor) throws FileNotFoundException {
        for (LocaleMapping locale : getOpts().getLocaleMapList()) {
            File transFile = new TransFileResolver(getOpts()).getTransFile(
                    DocNameWithoutExt.from(docName),
                    locale);
            if (transFile.exists()) {
                TranslationsResource targetDoc =
                        reader.extractTarget(transFile);
                visitor.visit(locale, targetDoc);
            }
        }
    }
}
