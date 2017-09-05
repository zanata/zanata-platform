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
package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("hTextFlowTargetStreamingDAO")
@RequestScoped
public class HTextFlowTargetStreamingDAO
        extends AbstractDAOImpl<HTextFlowTarget, Long> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(HTextFlowTargetStreamingDAO.class);
    private static final long serialVersionUID = -1L;

    public HTextFlowTargetStreamingDAO() {
        super(HTextFlowTarget.class);
    }

    public HTextFlowTargetStreamingDAO(Class<HTextFlowTarget> clz,
            Session session) {
        super(clz, session);
    }

    /**
     * @return scrollable result set of HTextFlowTarget which has all
     *         fields(locale, textflow, document, document locale, project
     *         iteration and project) eagerly fetched.
     */
    public ScrollableResults getAllTargetsWithAllFieldsEagerlyFetched() {
        Query query = getSession().createQuery(
                "from HTextFlowTarget tft join fetch tft.locale join fetch tft.textFlow join fetch tft.textFlow.document join fetch tft.textFlow.document.locale join fetch tft.textFlow.document.projectIteration join fetch tft.textFlow.document.projectIteration.project");
        query.setFetchSize(Integer.MIN_VALUE);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * @return scrollable result set of HTextFlowTarget under a project, with
     *         all of its fields(locale, textflow, document, document locale,
     *         project iteration and project) eagerly fetched.
     */
    public ScrollableResults
            getTargetsWithAllFieldsEagerlyFetchedForProject(HProject project) {
        Query query = getSession().createQuery(
                "from HTextFlowTarget tft join fetch tft.locale join fetch tft.textFlow join fetch tft.textFlow.document join fetch tft.textFlow.document.locale join fetch tft.textFlow.document.projectIteration join fetch tft.textFlow.document.projectIteration.project p where p = :project");
        return query.setFetchSize(Integer.MIN_VALUE)
                .setParameter("project", project)
                .scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * @return scrollable result set of HTextFlowTarget under a project
     *         iteration, with all of its fields(locale, textflow, document,
     *         document locale, project iteration and project) eagerly fetched.
     */
    public ScrollableResults
            getTargetsWithAllFieldsEagerlyFetchedForProjectIteration(
                    HProjectIteration iteration) {
        Query query = getSession().createQuery(
                "from HTextFlowTarget tft join fetch tft.locale join fetch tft.textFlow join fetch tft.textFlow.document join fetch tft.textFlow.document.locale join fetch tft.textFlow.document.projectIteration iter join fetch tft.textFlow.document.projectIteration.project where iter = :iteration");
        return query.setFetchSize(Integer.MIN_VALUE)
                .setParameter("iteration", iteration)
                .scroll(ScrollMode.FORWARD_ONLY);
    }
}
