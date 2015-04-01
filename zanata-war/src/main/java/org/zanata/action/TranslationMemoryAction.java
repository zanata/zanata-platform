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
import java.util.concurrent.Future;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
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
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationMemoryAction")
@Restrict("#{s:hasRole('admin')}")
@Scope(ScopeType.PAGE)
@Slf4j
public class TranslationMemoryAction implements Serializable {
    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private TransMemoryDAO transMemoryDAO;

    @In
    private TranslationMemoryResourceService translationMemoryResource;

    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    private List<TransMemory> transMemoryList;

    /**
     * Stores the last process handle, in page scope (ie for this user).
     */
    @In(scope = ScopeType.PAGE, required = false)
    @Out(scope = ScopeType.PAGE, required = false)
    private Future lastTaskResult;

    @Getter
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
        AsyncTaskHandle handle = new AsyncTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle,
                new ClearTransMemoryProcessKey(transMemorySlug));
        translationMemoryResource
                .deleteTranslationUnitsUnguardedAsync(transMemorySlug, handle);
        transMemoryList = null; // Force refresh next time list is requested
    }

    private boolean isProcessing() {
        return lastTaskResult != null;
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
        if (myProcessError != null)
            return myProcessError;
        if (lastTaskResult != null && lastTaskResult.isDone()) {
            try {
                lastTaskResult.get();
            } catch (InterruptedException e) {
                // no error, just interrupted
            } catch (ExecutionException e) {
                // remember the result, just until this event finishes
                this.myProcessError =
                        e.getCause() != null ? e.getCause().getMessage() : "";
            }
            lastTaskResult = null;
            return myProcessError;
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
        AsyncTaskHandle<Void> handle =
                asyncTaskHandleManager.getHandleByKey(
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
    @AllArgsConstructor
    @EqualsAndHashCode
    private class ClearTransMemoryProcessKey implements Serializable {
        private String slug;
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

            if (selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                return o1.getSlug().compareToIgnoreCase(o2.getSlug());
            } else {
                return o1.getCreationDate().compareTo(o2.getCreationDate());
            }
        }
    }
}
