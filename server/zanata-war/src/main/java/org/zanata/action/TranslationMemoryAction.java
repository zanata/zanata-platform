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
package org.zanata.action;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.CheckRole;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.exception.EntityMissingException;
import org.zanata.model.tm.TransMemory;
import org.zanata.rest.service.TranslationMemoryResourceService;
import org.zanata.ui.faces.FacesMessages;
import com.google.common.collect.Lists;

/**
 * Controller class for the Translation Memory UI.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("translationMemoryAction")
@CheckRole("admin")
@ViewScoped
@Model
@Transactional
public class TranslationMemoryAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationMemoryAction.class);

    private static final long serialVersionUID = -6791743907133760028L;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private TransMemoryDAO transMemoryDAO;
    @Inject
    private TranslationMemoryResourceService translationMemoryResource;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    private List<TransMemory> transMemoryList;
    private ClearTransMemoryProcessKey lastTaskTMKey;
    private SortingType tmSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.CREATED_DATE));
    private final TMComparator tmComparator =
            new TMComparator(getTmSortingList());

    /**
     * Stores the last process error, but only for the duration of the event.
     */
    private String myProcessError;

    public List<TransMemory> getAllTranslationMemories() {
        if (transMemoryList == null) {
            transMemoryList = transMemoryDAO.findAll();
        }
        return transMemoryList;
    }

    public void sortTMList() {
        Collections.sort(transMemoryList, tmComparator);
    }

    public void clearTransMemory(final String transMemorySlug) {
        lastTaskTMKey = new ClearTransMemoryProcessKey(transMemorySlug);
        AsyncTaskHandle handle = new AsyncTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle, lastTaskTMKey);
        translationMemoryResource
                .deleteTranslationUnitsUnguardedAsync(transMemorySlug, handle);
        transMemoryList = null; // Force refresh next time list is requested
    }

    private boolean isProcessing() {
        if (lastTaskTMKey != null) {
            AsyncTaskHandle<Void> handle =
                    asyncTaskHandleManager.getHandleByKey(lastTaskTMKey);
            return handle != null && !handle.isDone();
        }
        return false;
    }

    public boolean isProcessErrorPollEnabled() {
        // No need to poll just for process erorrs if we are already polling the
        // table
        return isProcessing() && !isTablePollEnabled();
    }

    /**
     * Gets the error (if any) for this user's last Clear operation, if it
     * finished since the last poll. NB: If the process has just finished, this
     * method will return the error only until the event scope exists.
     *
     * @return
     */
    public String getProcessError() {
        if (myProcessError != null) {
            return myProcessError;
        }
        if (lastTaskTMKey != null) {
            AsyncTaskHandle<Void> handle =
                    asyncTaskHandleManager.getHandleByKey(lastTaskTMKey);
            if (handle != null && handle.isDone()) {
                try {
                    handle.getResult();
                } catch (InterruptedException e) {
                    // no error, just interrupted
                } catch (ExecutionException e) {
                    // remember the result, just until this event finishes
                    this.myProcessError = e.getCause() != null
                            ? e.getCause().getMessage() : "";
                }
                lastTaskTMKey = null;
                return myProcessError;
            }
        }
        return "";
    }

    @Transactional
    public void deleteTransMemory(String transMemorySlug) {
        try {
            translationMemoryResource.deleteTranslationMemory(transMemorySlug);
            transMemoryList = null; // Force refresh next time list is requested
        } catch (EntityMissingException e) {
            facesMessages.addFromResourceBundle(SEVERITY_ERROR,
                    "jsf.transmemory.TransMemoryNotFound");
        }
    }

    public boolean isTransMemoryBeingCleared(String transMemorySlug) {
        AsyncTaskHandle<Void> handle = asyncTaskHandleManager.getHandleByKey(
                new ClearTransMemoryProcessKey(transMemorySlug));
        return handle != null && !handle.isDone();
    }

    public boolean deleteTransMemoryDisabled(String transMemorySlug) {
        // Translation memories have to be cleared before deleting them
        return getTranslationMemorySize(transMemorySlug) > 0;
    }

    public boolean isTablePollEnabled() {
        // Poll is enabled only when there is something being cleared
        for (TransMemory tm : getAllTranslationMemories()) {
            if (isTransMemoryBeingCleared(tm.getSlug())) {
                return true;
            }
        }
        return false;
    }

    public boolean isTranslationMemoryEmpty(String tmSlug) {
        return getTranslationMemorySize(tmSlug) <= 0;
    }

    public long getTranslationMemorySize(String tmSlug) {
        return transMemoryDAO.getTranslationMemorySize(tmSlug);
    }

    public String cancel() {
        // Navigation logic in pages.xml
        return "cancel";
    }

    /**
     * Represents a key to index a translation memory clear process.
     *
     * NB: Eventually this class might need to live outside if there are other
     * services that need to control this process.
     */
    private class ClearTransMemoryProcessKey implements Serializable {
        private String slug;

        @java.beans.ConstructorProperties({ "slug" })
        public ClearTransMemoryProcessKey(final String slug) {
            this.slug = slug;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof TranslationMemoryAction.ClearTransMemoryProcessKey))
                return false;
            final ClearTransMemoryProcessKey other =
                    (ClearTransMemoryProcessKey) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$slug = this.slug;
            final Object other$slug = other.slug;
            if (this$slug == null ? other$slug != null
                    : !this$slug.equals(other$slug))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof TranslationMemoryAction.ClearTransMemoryProcessKey;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $slug = this.slug;
            result = result * PRIME + ($slug == null ? 43 : $slug.hashCode());
            return result;
        }
    }

    private class TMComparator implements Comparator<TransMemory> {
        private SortingType sortingType;

        public TMComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(TransMemory o1, TransMemory o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                TransMemory temp = o1;
                o1 = o2;
                o2 = temp;
            }
            if (selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                return o1.getSlug().compareToIgnoreCase(o2.getSlug());
            } else {
                return o1.getCreationDate().compareTo(o2.getCreationDate());
            }
        }
    }

    public SortingType getTmSortingList() {
        return this.tmSortingList;
    }
}
