/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.action;

import com.google.common.collect.Lists;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.transaction.TransactionUtil;

import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
@Named("mergeMTAction")
@ViewScoped
@Model
@Transactional
public class MergeMTAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MergeMTAction.class);

    private String projectSlug;
    private String versionSlug;

    @Inject
    private DocumentDAO documentDAO;

    @Inject
    private TransactionUtil transactionUtil;

    @Inject
    private MTService mtService;

    public void mergeMT() throws Exception {
        LocaleId targetLocale = new LocaleId("ja");

        List<HDocument> documentList = documentDAO
                .getByProjectIteration(projectSlug, versionSlug, false);

//        for (int docCount = 0; docCount < documentList.size(); docCount++) {
            HDocument doc = documentList.get(2);
            log.info("running doc #" + 2 + " of " + documentList.size());
            List<Long> ids =
                    doc.getTextFlows().stream().map(HTextFlow::getId).collect(
                            Collectors.toList());
            List<List<Long>> smallerLists = Lists.partition(ids, 5);

            for (List<Long> list: smallerLists) {
                transactionUtil.call(() -> mtService.merge(list, targetLocale));
            }
//        }
    }


    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getVersionSlug() {
        return versionSlug;
    }

    public void setVersionSlug(String versionSlug) {
        this.versionSlug = versionSlug;
    }
}
