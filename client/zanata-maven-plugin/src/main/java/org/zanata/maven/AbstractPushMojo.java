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

import javax.annotation.Nonnull;

import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.RawPushCommand;

import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
abstract class AbstractPushMojo extends
        AbstractPushPullMojo<PushOptions> implements PushOptions {

    /**
     * Language of source documents
     */
    @Parameter(property = "zanata.sourceLang", defaultValue = "en-US")
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
     * mergeType).
     * @deprecated Replaced by pushType=trans
     */
    @Parameter(property = "zanata.pushTrans")
    @Deprecated
    private String pushTrans;

    /**
     * Type of push to perform on the server: "source" pushes source documents
     * only. "trans" pushes translation documents only. "both" pushes both
     * source and translation documents.
     */
    @Parameter(property = "zanata.pushType", defaultValue = "source")
    private String pushType = "source";

    /**
     * Whether the server should copy latest translations from equivalent
     * messages/documents in the database
     */
    @Parameter(property = "zanata.copyTrans", defaultValue = "false")
    private boolean copyTrans;

    /**
     * Merge type: "auto" (default) or "import" (DANGER!).
     */
    @Parameter(property = "zanata.merge", defaultValue = "auto")
    private String merge = "auto";

    /**
     * Add default excludes (.svn, .git, etc) to the exclude filters.
     */
    @Parameter(property = "zanata.defaultExcludes", defaultValue = "true")
    private boolean defaultExcludes = true;

    /**
     * Maximum size, in bytes, of document chunks to transmit when using project
     * type 'file'. Documents smaller than this size will be transmitted in a
     * single request, larger documents will be sent over multiple requests.
     *
     * Usage -Dzanata.maxChunkSize=12345
     */
    @Parameter(property = "zanata.maxChunkSize", defaultValue = "1048576")
    private int maxChunkSize = 1024 * 1024;

    /**
     * File types to locate and transmit to the server
     * when using project type "file".
     * NOTE: No file types will be pushed unless they are listed here.
     * <p>
     * Pattern: TYPE[extension;extension],TYPE[extension],TYPE
     * </p>
     * <p>
     * The default file extension(s) for each TYPE will be used unless
     * 'extension' is specified in square brackets. If overriding extensions
     * from the command line, please note that most shells require quotes
     * around square brackets and semicolons unless they are escaped.
     * </p>
     * <p>
     * Example: -Dzanata.fileTypes="PROPERTIES,PLAIN_TEXT[md;txt]"
     * </p>
     * <p>
     * Use push with the option -Dzanata.listFileTypes to see the server's
     * supported types and their default extensions.
     * </p>
     */
    @Parameter(property = "zanata.fileTypes")
    private String[] fileTypes = new String[0];

    /**
     * List file types supported by the configured server, instead
     * of pushing files.
     */
    @Parameter(property = "zanata.listFileTypes")
    private boolean listFileTypes = false;

    /**
     * Case sensitive for includes and excludes options.
     */
    @Parameter(property = "zanata.caseSensitive", defaultValue = "true")
    private boolean caseSensitive = true;

    /**
     * Exclude filenames which match locales configured for the project (other
     * than the source locale). For instance, if project includes de and fr,
     * then the files messages_de.properties and messages_fr.properties will not
     * be treated as source files.
     * <p>
     * NB: This parameter will be ignored for some project types which use
     * different file naming conventions (eg podir, gettext).
     */
    @Parameter(property = "zanata.excludeLocaleFilenames", defaultValue = "true")
    private boolean excludeLocaleFilenames = true;

    /**
     * Run validation check against file. Only applies to XLIFF project type.
     * "CONTENT" - content validation check (quick). "XSD" - validation check
     * against xliff 1.1 schema -
     * http://www.oasis-open.org/committees/xliff/documents/xliff-core-1.1.xsd.
     */
    @Parameter(property = "zanata.validate", defaultValue = "content")
    private String validate = "content";

    /**
     * Indicates if all uploaded translations were translated by you.
     */
    @Parameter(property = "zanata.myTrans", defaultValue = "false")
    private boolean myTrans = false;

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
    public boolean getListFileTypes() {
        return listFileTypes;
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
    @Nonnull
    public String getValidate() {
        return validate;
    }

    @Override
    public boolean isMyTrans() {
        return myTrans;
    }
}
