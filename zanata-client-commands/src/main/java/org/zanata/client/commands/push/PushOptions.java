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

import com.google.common.collect.ImmutableList;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.client.commands.PushPullType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import javax.annotation.Nonnull;

/**
 * Push options for documents that are parsed on the client and transmitted as a
 * {@link Resource} or {@link TranslationsResource}.
 */
public interface PushOptions extends PushPullOptions {
    public String getSourceLang();

    public PushPullType getPushType();

    public String getMergeType();

    public boolean getCaseSensitive();

    public boolean getExcludeLocaleFilenames();

    public boolean getDefaultExcludes();

    public boolean getDeleteObsoleteModules();

    public boolean getCopyTrans();

    // raw file push
    public int getChunkSize();

    public ImmutableList<String> getFileTypes();

    @Nonnull
    public String getValidate();

    public boolean isMyTrans();

}
