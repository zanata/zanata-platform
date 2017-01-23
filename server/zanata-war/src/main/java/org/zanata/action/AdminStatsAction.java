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
package org.zanata.action;

import java.io.Serializable;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("adminStatsAction")
@ViewScoped
@Model
@Transactional
public class AdminStatsAction implements Serializable {

    /**
     */
    private static final long serialVersionUID = 1L;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private DocumentDAO documentDAO;
    private boolean documentReady;

    public int getTotalProjectCount() {
        return projectDAO.getTotalProjectCount();
    }

    public int getTotalActiveProjectCount() {
        return projectDAO.getTotalActiveProjectCount();
    }

    public int getTotalReadOnlyProjectCount() {
        return projectDAO.getTotalReadOnlyProjectCount();
    }

    public int getTotalObsoleteProjectCount() {
        return projectDAO.getTotalObsoleteProjectCount();
    }

    public int getTotalProjectIterCount() {
        return projectIterationDAO.getTotalProjectIterCount();
    }

    public int getTotalActiveProjectIterCount() {
        return projectIterationDAO.getTotalActiveProjectIterCount();
    }

    public int getTotalReadOnlyProjectIterCount() {
        return projectIterationDAO.getTotalReadOnlyProjectIterCount();
    }

    public int getTotalObsoleteProjectIterCount() {
        return projectIterationDAO.getTotalObsoleteProjectIterCount();
    }

    public int getTotalTranslator() {
        return personDAO.getTotalTranslator();
    }

    public int getTotalReviewer() {
        return personDAO.getTotalReviewer();
    }

    public int getTotalDocuments() {
        return documentDAO.getTotalDocument();
    }

    public int getTotalActiveDocuments() {
        return documentDAO.getTotalActiveDocument();
    }

    public int getTotalObsoleteDocuments() {
        return documentDAO.getTotalObsoleteDocument();
    }

    public int getTotalTextFlows() {
        return textFlowDAO.getTotalTextFlows();
    }

    public int getTotalActiveTextFlows() {
        return textFlowDAO.getTotalActiveTextFlows();
    }

    public int getTotalObsoleteTextFlows() {
        return textFlowDAO.getTotalObsoleteTextFlows();
    }

    public int getTotalTextFlowTargets() {
        return textFlowTargetDAO.getTotalTextFlowTargets();
    }

    public int getTotalActiveTextFlowTargets() {
        return textFlowTargetDAO.getTotalActiveTextFlowTargets();
    }

    public int getTotalObsoleteTextFlowTargets() {
        return textFlowTargetDAO.getTotalObsoleteTextFlowTargets();
    }

    public int getTotalApprovedOrTranslatedTextFlowTargets() {
        return textFlowTargetDAO.getTotalApprovedOrTranslatedTextFlowTargets();
    }

    public int getTotalRejectedOrFuzzyTextFlowTargets() {
        return textFlowTargetDAO.getTotalRejectedOrFuzzyTextFlowTargets();
    }

    public int getTotalUntranslatedTextFlowTargets() {
        return textFlowTargetDAO.getTotalNewTextFlowTargets();
    }

    public boolean isDocumentReady() {
        return this.documentReady;
    }

    public void setDocumentReady(final boolean documentReady) {
        this.documentReady = documentReady;
    }
}
