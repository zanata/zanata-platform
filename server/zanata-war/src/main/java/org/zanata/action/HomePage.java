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
package org.zanata.action;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.events.HomeContentChangedEvent;
import org.zanata.util.CommonMarkRenderer;

/**
 * This component caches the latest HTML version of the home page's content.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("homePage")
@ApplicationScoped
public class HomePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(HomePage.class);

    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private CommonMarkRenderer renderer;
    private String html;

    /**
     * Returns the rendered, sanitised HTML for the home page content set by
     * admin.
     *
     * @return
     */
    @Transactional(readOnly = true)
    public String getHtml() {
        if (html == null) {
            String text = applicationConfiguration.getHomeContent();
            if (text == null) {
                html = "";
            } else {
                html = renderer.renderToHtmlSafe(text);
            }
        }
        return html;
    }

    /**
     * Event handler to clear the cached HTML based on the latest CommonMark
     * home content.
     */
    public void clearHtml(@Observes(
            during = TransactionPhase.AFTER_SUCCESS) HomeContentChangedEvent event) {
        html = null;
    }
}
