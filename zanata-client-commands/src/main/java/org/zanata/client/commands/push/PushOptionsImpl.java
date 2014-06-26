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

import com.google.common.collect.ImmutableList;
import org.kohsuke.args4j.Option;
import org.zanata.client.commands.AbstractPushPullOptionsImpl;
import org.zanata.client.commands.BooleanValueHandler;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.util.StringUtil;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PushOptionsImpl extends AbstractPushPullOptionsImpl<PushOptions>
        implements PushOptions {
    private static final boolean DEF_EXCLUDES = true;
    private static final boolean DEF_CASE_SENSITIVE = true;
    private static final boolean DEF_EXCLUDE_LOCALES = true;
    private static final boolean DEF_COPYTRANS = true;
    private static final int DEF_CHUNK_SIZE = 1024 * 1024;
    /** @see org.zanata.common.MergeType for options */
    private static final String DEF_MERGE_TYPE = "AUTO";
    private static final String DEF_PUSH_TYPE = "source";

    private ImmutableList<String> includes = ImmutableList.of();
    private ImmutableList<String> excludes = ImmutableList.of();
    private ImmutableList<String> fileTypes = ImmutableList.of();
    private boolean defaultExcludes = DEF_EXCLUDES;
    private String mergeType = DEF_MERGE_TYPE;
    private boolean caseSensitive = DEF_CASE_SENSITIVE;
    private int chunkSize = DEF_CHUNK_SIZE;
    private boolean excludeLocaleFilenames = DEF_EXCLUDE_LOCALES;
    private boolean copyTrans = DEF_COPYTRANS;
    private String pushType = DEF_PUSH_TYPE;
    private String sourceLang = "en-US";

    private String validate;

    @Override
    public ZanataCommand initCommand() {
        if (PROJECT_TYPE_FILE.equalsIgnoreCase(getProjectType())) {
            return new RawPushCommand(this);
        } else {
            return new PushCommand(this);
        }
    }

    @Override
    public String getCommandName() {
        return "push";
    }

    @Override
    public String getCommandDescription() {
        return "Pushes source text to a Zanata project version so that it can be translated.";
    }

    @Override
    public String getSourceLang() {
        return sourceLang;
    }

    @Option(name = "--src-lang",
            usage = "Language of source documents (defaults to en-US)")
    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    @Option(aliases = { "-l" }, name = "--locales",
            metaVar = "LOCALE1,LOCALE2,...",
            usage = "Locales to push to the server.\n"
                    + "By default all locales in zanata.xml will be pushed.")
    public void setLocales(String locales) {
        this.locales = locales.split(",");
    }

    @Override
    public boolean getCopyTrans() {
        return copyTrans;
    }

    public boolean isCopyTrans() {
        return copyTrans;
    }

    @Option(
            name = "--copy-trans",
            handler = BooleanValueHandler.class,
            usage = "Copy latest translations from equivalent messages/documents in the database (default: "
                    + DEF_COPYTRANS + ")")
    public
            void setCopyTrans(boolean copyTrans) {
        this.copyTrans = copyTrans;
    }

    @Override
    public String getMergeType() {
        return mergeType;
    }

    @Option(name = "--merge-type", metaVar = "TYPE",
            usage = "Merge type: \"auto\" (default) or \"import\" (DANGER!).")
    public void setMergeType(String mergeType) {
        this.mergeType = mergeType;
    }

    @Override
    public PushPullType getPushType() {
        return PushPullType.fromString(pushType);
    }

    @Option(
            name = "--push-type",
            metaVar = "TYPE",
            required = false,
            usage = "Type of push to perform on the server:\n"
                    + "  \"source\" (default) pushes source documents only.\n"
                    + "  \"trans\" pushes translation documents only.\n"
                    + "  \"both\" pushes both source and translation documents.")
    public
            void setPushType(String pushType) {
        this.pushType = pushType;
    }

    @Override
    public ImmutableList<String> getIncludes() {
        return includes;
    }

    @Option(
            name = "--includes",
            metaVar = "INCLUDES",
            usage = "Wildcard pattern to include files and directories. This parameter is only\n"
                    + "needed for some project types, eg XLIFF, Properties. Usage\n"
                    + "--includes=\"src/myfile*.xml,**/*.xlf\"")
    public
            void setIncludes(String includes) {
        this.includes = ImmutableList.copyOf(StringUtil.split(includes, ","));
    }

    @Override
    public ImmutableList<String> getExcludes() {
        return excludes;
    }

    @Option(
            name = "--excludes",
            metaVar = "EXCLUDES",
            usage = "Wildcard pattern to exclude files and directories. Usage\n"
                    + "--excludes=\"Pattern1,Pattern2,Pattern3\"")
    public
            void setExcludes(String excludes) {
        this.excludes = ImmutableList.copyOf(StringUtil.split(excludes, ","));
    }

    @Override
    public boolean getDefaultExcludes() {
        return defaultExcludes;
    }

    @Option(
            name = "--default-excludes",
            handler = BooleanValueHandler.class,
            usage = "Add the default excludes (.svn, .git, etc) to the excludes list (default: "
                    + DEF_EXCLUDES + ")")
    public
            void setDefaultExcludes(boolean defaultExcludes) {
        this.defaultExcludes = defaultExcludes;
    }

    @Override
    public boolean getDeleteObsoleteModules() {
        // modules are currently only supported by Maven Mojos:
        return false;
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Option(
            name = "--chunk-size",
            metaVar = "SIZE",
            usage = "Maximum size, in bytes, of document chunks to transmit. Documents smaller\n"
                    + "than this size will be transmitted in a single request, larger documents\n"
                    + "will be sent over multiple requests.")
    public
            void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public ImmutableList<String> getFileTypes() {
        return fileTypes;
    }

    @Option(name = "--file-types", metaVar = "TYPES",
            usage = "File types to locate and transmit to the server.")
    public void setFileTypes(String fileTypes) {
        this.fileTypes = ImmutableList.copyOf(StringUtil.split(fileTypes, ","));
    }

    @Override
    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    @Option(
            name = "--case-sensitive",
            handler = BooleanValueHandler.class,
            usage = "Consider case of filenames in includes and excludes options. (default: "
                    + DEF_CASE_SENSITIVE + ")")
    public
            void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean getExcludeLocaleFilenames() {
        return excludeLocaleFilenames;
    }

    @Option(
            name = "--exclude-locale-filenames",
            handler = BooleanValueHandler.class,
            usage = "Exclude filenames which match locales in zanata.xml (other than the\n"
                    + "source locale).  For instance, if zanata.xml includes de and fr,\n"
                    + "then the files messages_de.properties and messages_fr.properties\n"
                    + "will not be treated as source files.\n"
                    + "NB: This parameter will be ignored for some project types which use\n"
                    + "different file naming conventions (eg podir, gettext).\n"
                    + "(default: " + DEF_EXCLUDE_LOCALES + ")")
    public
            void setExcludeLocaleFilenames(boolean excludeLocaleFilenames) {
        this.excludeLocaleFilenames = excludeLocaleFilenames;
    }

    @Override
    public String getValidate() {
        return validate;
    }

    @Option(
            name = "--validate",
            metaVar = "TYPE",
            usage = "Type of validation for XLIFF files. (values: XSD, CONTENT (default))")
    public
            void setValidate(String validate) {
        this.validate = validate;
    }

}
