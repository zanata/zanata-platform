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

package org.zanata.maven;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.RawPushCommand;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public abstract class AbstractPushMojo extends
        AbstractPushPullMojo<PushOptions> implements PushOptions {

    /**
     * Language of source documents
     *
     * @parameter expression="${zanata.sourceLang}" default-value="en-US"
     */
    private String sourceLang = "en-US";

    @Override
    public PushPullCommand<PushOptions> initCommand() {
        if (PROJECT_TYPE_FILE.equalsIgnoreCase(getProjectType())) {
            return new RawPushCommand(this);
        } else {
            return new PushCommand(this);
        }
    }

    /**
     * Push translations from local files to the server (merge or import: see
     * mergeType). This option is deprecated, replaced by pushType.
     *
     * @parameter expression="${zanata.pushTrans}"
     */
    @Deprecated
    private String pushTrans;

    /**
     * Type of push to perform on the server: "source" pushes source documents
     * only. "trans" pushes translation documents only. "both" pushes both
     * source and translation documents.
     *
     * @parameter expression="${zanata.pushType}" default-value="source"
     */
    private String pushType;

    /**
     * Whether the server should copy latest translations from equivalent
     * messages/documents in the database
     *
     * @parameter expression="${zanata.copyTrans}" default-value="true"
     */
    private boolean copyTrans;

    /**
     * Merge type: "auto" (default) or "import" (DANGER!).
     *
     * @parameter expression="${zanata.merge}" default-value="auto"
     */
    private String merge;

    /**
     * Wildcard pattern to include files and directories. This parameter is only
     * needed for some project types, eg XLIFF, Properties. Usage
     * -Dzanata.includes="src/myfile*.xml,**&#47*.xliff.xml"
     *
     * @parameter expression="${zanata.includes}"
     */
    private String includes;

    /**
     * Wildcard pattern to exclude files and directories. Usage
     * -Dzanata.excludes="Pattern1,Pattern2,Pattern3"
     *
     * @parameter expression="${zanata.excludes}"
     */
    private String excludes;

    /**
     * Add default excludes (.svn, .git, etc) to the exclude filters.
     *
     * @parameter expression="${zanata.defaultExcludes}" default-value="true"
     */
    private boolean defaultExcludes = true;

    /**
     * Maximum size, in bytes, of document chunks to transmit when using project
     * type 'file'. Documents smaller than this size will be transmitted in a
     * single request, larger documents will be sent over multiple requests.
     *
     * Usage -Dzanata.maxChunkSize=12345
     *
     * @parameter expression="${zanata.maxChunkSize}" default-value="1048576"
     */
    private int maxChunkSize = 1024 * 1024;

    /**
     * File types to locate and transmit to the server when using project type
     * "file".
     *
     * @parameter expression="${zanata.fileTypes}"
     *            default-value="txt,dtd,odt,fodt,odp,fodp,ods,fods,odg,fodg,odf,odb"
     */
    private String[] fileTypes;

    /**
     * Case sensitive for includes and excludes options.
     *
     * @parameter expression="${zanata.caseSensitive}" default-value="true"
     */
    private boolean caseSensitive = true;

    /**
     * Exclude filenames which match locales in zanata.xml (other than the
     * source locale). For instance, if zanata.xml includes de and fr, then the
     * files messages_de.properties and messages_fr.properties will not be
     * treated as source files.
     * <p>
     * NB: This parameter will be ignored for some project types which use
     * different file naming conventions (eg podir, gettext).
     *
     * @parameter expression="${zanata.excludeLocaleFilenames}"
     *            default-value="true"
     */
    private boolean excludeLocaleFilenames = true;

    /**
     * Run validation check against file. Only applies to XLIFF project type.
     * "CONTENT" - content validation check (quick). "XSD" - validation check
     * against xliff 1.1 schema -
     * http://www.oasis-open.org/committees/xliff/documents/xliff-core-1.1.xsd.
     *
     * @parameter expression="${zanata.validate}" default-value="content"
     */
    private String validate = "content";

    @Override
    public String getSourceLang() {
        return sourceLang;
    }

    @Override
    public PushPullType getPushType() {
        // if the deprecated 'pushTrans' option has been used
        if (pushTrans != null) {
            return Boolean.parseBoolean(pushTrans) ? PushPullType.Both
                    : PushPullType.Source;
        } else {
            return PushPullType.fromString(pushType);
        }
    }

    @Override
    public boolean getCopyTrans() {
        return copyTrans;
    }

    @Override
    public String getMergeType() {
        return merge;
    }

    @Override
    public ImmutableList<String> getIncludes() {
        if (includes != null) {
            String[] includeList = StringUtils.split(includes, ",");
            return ImmutableList.copyOf(includeList);
        }
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<String> getExcludes() {
        if (excludes != null) {
            String[] excludeList = StringUtils.split(excludes, ",");
            return ImmutableList.copyOf(excludeList);
        }
        return ImmutableList.of();
    }

    @Override
    public boolean getDefaultExcludes() {
        return defaultExcludes;
    }

    @Override
    public int getChunkSize() {
        return maxChunkSize;
    }

    @Override
    public ImmutableList<String> getFileTypes() {
        return ImmutableList.copyOf(fileTypes);
    }

    @Override
    public String getCommandName() {
        return "push";
    }

    @Override
    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public boolean getExcludeLocaleFilenames() {
        return excludeLocaleFilenames;
    }

    @Override
    public String getValidate() {
        return validate;
    }

}
