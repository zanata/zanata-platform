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

import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.RawPullCommand;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public abstract class AbstractPullMojo extends
        AbstractPushPullMojo<PullOptions> implements PullOptions {
    /**
     * Export source-language text from Zanata to local files, overwriting or
     * erasing existing files (DANGER!). This option is deprecated, replaced by
     * pullType.
     *
     * @parameter expression="${zanata.pullSrc}"
     */
    @Deprecated
    private String pullSrc;

    /**
     * Whether to create skeleton entries for strings/files which have not been
     * translated yet
     *
     * @parameter expression="${zanata.createSkeletons}"
     */
    private boolean createSkeletons;

    /**
     * Whether to include fuzzy translations in translation files when using
     * project type 'file'. If this option is false, source text will be used
     * for any string that does not have an approved translation.
     *
     * @parameter expression="${zanata.includeFuzzy}" default-value="false"
     */
    private boolean includeFuzzy = false;

    /**
     * Whether to purge the cache before performing the pull operation. This
     * means that all documents will be fetched from the server anew.
     *
     * @parameter expression="${zanata.purgeCache}" default-value="false"
     */
    private boolean purgeCache;

    /**
     * Whether to use an Entity cache when fetching documents. When using the
     * cache, documents that have been retrieved previously and have not changed
     * since then will not be retrieved again.
     *
     * @parameter expression="${zanata.useCache}" default-value="true"
     */
    private boolean useCache;

    /**
     * Type of pull to perform from the server: "source" pulls source documents
     * only. "trans" pulls translation documents only. "both" pulls both source
     * and translation documents.
     *
     * @parameter expression="${zanata.pullType}" default-value="trans"
     */
    private String pullType;

    /**
     * Whether tabs should be encoded as \t (true) or left as tabs (false).
     *
     * @parameter expression="${zanata.encodeTabs}" default-value="true"
     */
    private boolean encodeTabs = true;

    /**
     * When there is an error, whether try to workaround it and continue to next
     * text flow or fail the process. i.e. when encounter a mismatch plural
     * form, it will try to use singular form. Note: This option may not work on
     * all circumstances.
     *
     * @parameter expression="${zanata.continueAfterError}"
     *            default-value="false"
     */
    private boolean continueAfterError = false;

    /**
     * Accepts integer from 0 to 100. Only pull translation documents which are
     * at least PERCENT % completed. Please note specifying this option may
     * cause longer time to pull for a large project.
     *
     * @parameter expression="${zanata.minDocPercent}" default-value="0"
     */
    private int minDocPercent = 0;

    /**
    *
    */
    public AbstractPullMojo() {
        super();
        Preconditions
                .checkArgument(minDocPercent >= 0 && minDocPercent <= 100,
                        "zanata.minDocPercent should be an integer from 0 to 100");
    }

    public PushPullCommand<PullOptions> initCommand() {
        if (PROJECT_TYPE_FILE.equalsIgnoreCase(getProjectType())) {
            return new RawPullCommand(this);
        } else {
            return new PullCommand(this);
        }
    }

    @Override
    public boolean getCreateSkeletons() {
        return createSkeletons;
    }

    @Override
    public boolean getEncodeTabs() {
        return encodeTabs;
    }

    @Override
    public boolean getIncludeFuzzy() {
        return includeFuzzy;
    }

    @Override
    public boolean getPurgeCache() {
        return purgeCache;
    }

    @Override
    public boolean getUseCache() {
        return useCache;
    }

    @Override
    public boolean isContinueAfterError() {
        return continueAfterError;
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD",
            justification = "Injected by Maven")
    @Override
    public PushPullType getPullType() {
        // if the deprecated 'pushTrans' option has been used
        if (pullSrc != null) {
            return Boolean.parseBoolean(pullSrc) ? PushPullType.Both
                    : PushPullType.Trans;
        } else {
            return PushPullType.fromString(pullType);
        }
    }

    @Override
    public String getCommandName() {
        return "pull";
    }

    @Override
    public boolean isAuthRequired() {
        return false;
    }

    @Override
    public int getMinDocPercent() {
        return minDocPercent;
    }
}
