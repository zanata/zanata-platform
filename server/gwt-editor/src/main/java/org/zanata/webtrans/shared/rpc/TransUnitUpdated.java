/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.shared.rpc;

import com.google.common.base.MoreObjects;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import com.google.common.base.Objects;

//@ExposeEntity
public class TransUnitUpdated implements SessionEventData,
        HasTransUnitUpdatedData {

    public enum UpdateType {
        // save as Translated or Fuzzy for an individual message
        WebEditorSave,
        // all other type of saving. Include: revert, TM merge, search & replace, non webtrans actions like REST, copyTrans
        NonEditorSave
    }

    private static final long serialVersionUID = 1L;
    private TransUnitUpdateInfo tuUpdateInfo;
    private EditorClientId updatedInSession;
    private UpdateType updateType;

    // for ExposeEntity
    public TransUnitUpdated() {
    }

    public TransUnitUpdated(TransUnitUpdateInfo tuUpdateInfo,
            EditorClientId updatedInSession, UpdateType updateType) {
        this.tuUpdateInfo = tuUpdateInfo;
        this.updatedInSession = updatedInSession;
        this.updateType = updateType;
    }

    @Override
    public TransUnitUpdateInfo getUpdateInfo() {
        return tuUpdateInfo;
    }

    @Override
    public EditorClientId getEditorClientId() {
        return updatedInSession;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tuUpdateInfo", tuUpdateInfo)
                .add("updatedInSession", updatedInSession)
                .add("updateType", updateType)
                .toString();
    }
}
