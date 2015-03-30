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

package org.zanata.client.commands.pull;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.AbstractPushPullOptionsImpl;
import org.zanata.client.commands.BooleanValueHandler;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.PushPullType;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PullOptionsImpl extends AbstractPushPullOptionsImpl<PullOptions>
        implements PullOptions {
    private static final String DEFAULT_PULL_TYPE = "trans";
    private static final boolean DEFAULT_CREATE_SKELETONS = false;
    private static final boolean DEFAULT_ENCODE_TABS = true;
    private static final boolean DEFAULT_INCLUDE_FUZZY = false;
    private static final boolean DEFAULT_USE_CACHE = true;
    private static final boolean DEFAULT_PURGE_CACHE = false;
    private static final boolean DEFAULT_CONTINUE_AFTER_ERROR = false;

    private String pullType = DEFAULT_PULL_TYPE;

    private boolean createSkeletons = DEFAULT_CREATE_SKELETONS;
    private boolean encodeTabs = DEFAULT_ENCODE_TABS;
    private boolean includeFuzzy = DEFAULT_INCLUDE_FUZZY;
    private boolean useCache = DEFAULT_USE_CACHE;
    private boolean purgeCache = DEFAULT_PURGE_CACHE;
    private boolean continueAfterError = DEFAULT_CONTINUE_AFTER_ERROR;

    @Override
    public ZanataCommand initCommand() {
        if (PROJECT_TYPE_FILE.equalsIgnoreCase(getProjectType())) {
            return new RawPullCommand(this);
        } else {
            return new PullCommand(this);
        }
    }

    @Override
    public String getCommandName() {
        return "pull";
    }

    @Override
    public String getCommandDescription() {
        return "Pull translated text from Zanata.";
    }

    @Option(aliases = { "-l" }, name = "--locales",
            metaVar = "LOCALE1,LOCALE2",
            usage = "Locales to pull from the server.\n"
                    + "By default all locales configured will be pulled.")
    public void setLocales(String locales) {
        this.locales = locales.split(",");
    }

    @Override
    public PushPullType getPullType() {
        return PushPullType.fromString(pullType);
    }

    @Option(
            name = "--pull-type",
            metaVar = "TYPE",
            required = false,
            usage = "Type of pull to perform from the server: \"source\" pulls source documents only.\n"
                    + "\"trans\" (default) pulls translation documents only.\n"
                    + "\"both\" pulls both source and translation documents.")
    public
            void setPullType(String pullType) {
        this.pullType = pullType;
    }

    @Override
    public boolean getCreateSkeletons() {
        return createSkeletons;
    }

    @Option(
            name = "--create-skeletons",
            usage = "Create skeleton entries for strings/files which have not been translated yet."
                    + " Skeletons are not created by default.")
    public
            void setCreateSkeletons(boolean createSkeletons) {
        this.createSkeletons = createSkeletons;
    }

    @Override
    public boolean getEncodeTabs() {
        return this.encodeTabs;
    }

    @Option(
            name = "--encode-tabs",
            handler = BooleanValueHandler.class,
            usage = "Whether tabs should be encoded as \\t (true, default) or left as tabs (false).")
    public
            void setEncodeTabs(boolean encodeTabs) {
        this.encodeTabs = encodeTabs;
    }

    @Override
    public boolean getIncludeFuzzy() {
        return this.includeFuzzy;
    }

    @Option(
            name = "--include-fuzzy",
            usage = "[project type 'file' only] Whether to include fuzzy "
                    + "translations in translation files. "
                    + "If this option is false (default), source text will be used for any string "
                    + "that does not have an approved translation.")
    public
            void setIncludeFuzzy(boolean includeFuzzy) {
        this.includeFuzzy = includeFuzzy;
    }

    @Override
    public boolean getPurgeCache() {
        return purgeCache;
    }

    @Override
    public boolean getUseCache() {
        return useCache;
    }

    @Option(
            name = "--continue-after-error",
            aliases = "-c",
            usage = "When there is an error, whether try to workaround it and continue to next text flow or fail the process."
                    + "i.e. when encounter a mismatch plural form, it will try to use singular form.\n"
                    + "Note: This option may not work on all circumstances.")
    public
            void setContinueAfterError(boolean continueAfterError) {
        this.continueAfterError = continueAfterError;
    }

    @Override
    public boolean isContinueAfterError() {
        return continueAfterError;
    }

    @Override
    public boolean isAuthRequired() {
        return false;
    }
}
