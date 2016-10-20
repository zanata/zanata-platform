/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

package org.zanata.service;

import org.zanata.common.LocaleId;
import org.zanata.exception.RequestExistsException;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;
import org.zanata.model.type.RequestState;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface RequestService {

    /**
     * Create join language request
     *
     * @param requester
     * @param locale
     * @throws org.zanata.exception.RequestExistsException
     */
    LanguageRequest createLanguageRequest(HAccount requester,
            HLocale locale, boolean isRequestAsCoordinator,
            boolean isRequestAsReviewer,
            boolean isRequestAsTranslator) throws RequestExistsException;

    /**
     * Check if request already exist in the locale by this user
     * @param requester
     * @param locale
     */
    boolean doesLanguageRequestExist(HAccount requester, HLocale locale);

    /**
     * Update join language request
     * @param requestId - Language request Id
     * @param actor
     * @param state
     * @param comment
     * @throws EntityNotFoundException
     */
    void updateLanguageRequest(Long requestId, HAccount actor,
        RequestState state, String comment) throws EntityNotFoundException;

    /**
     * Get language request by id
     * @param languageRequestId
     */
    LanguageRequest getLanguageRequest(long languageRequestId);

    /**
     * Get pending language request for user
     *
     * @param localeId
     */
    LanguageRequest getPendingLanguageRequests(HAccount account,
        LocaleId localeId);

    /**
     *
     * @param Get outstanding language requests on languages
     */
    List<LanguageRequest> getPendingLanguageRequests(
        LocaleId... localeIds);

    /**
     * Return pending request by entityId
     * @param entityId
     */
    Request getPendingRequestByEntityId(String entityId);

    /**
     * Return request including history by entityId
     * @param entityId
     */
    List<Request> getRequestHistoryByEntityId(String entityId);
}
