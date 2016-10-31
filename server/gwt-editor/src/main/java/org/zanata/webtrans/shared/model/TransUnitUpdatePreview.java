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
package org.zanata.webtrans.shared.model;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.rpc.ReplaceText;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds preview information about a {@link ReplaceText} operation
 *
 * @author David Mason, damason@redhat.com
 *
 */
public class TransUnitUpdatePreview implements IsSerializable {
    private TransUnitId id;
    private List<String> contents;
    private ContentState state;

    @SuppressWarnings("unused")
    private TransUnitUpdatePreview() {
    }

    public TransUnitUpdatePreview(TransUnitId id, List<String> contents,
            ContentState state) {
        this.id = id;
        this.contents = contents;
        this.state = state;
    }

    public TransUnitId getId() {
        return id;
    }

    public List<String> getContents() {
        return contents;
    }

    public ContentState getState() {
        return state;
    }

}
