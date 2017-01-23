/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */
package org.zanata.ui;

import java.io.Serializable;
import java.util.List;
import org.zanata.seam.scope.ConversationScopeMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractAutocomplete<T> implements Serializable {

    protected ConversationScopeMessages conversationScopeMessages =
            ConversationScopeMessages.instance();
    private String query; // String of the input box
    private String selectedItem; // Selected item from the suggestion

    /**
     * Return results on search
     */
    public abstract List<T> suggest();

    /**
     * Action when an item is selected
     */
    public abstract void onSelectItemAction();

    public void reset() {
        selectedItem = "";
        query = "";
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    public void setSelectedItem(final String selectedItem) {
        this.selectedItem = selectedItem;
    }

    public String getSelectedItem() {
        return this.selectedItem;
    }
}
