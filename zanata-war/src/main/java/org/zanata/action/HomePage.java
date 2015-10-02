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

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import org.zanata.ApplicationConfiguration;
import org.zanata.events.HomeContentChangedEvent;
import org.zanata.util.CommonMarkRenderer;

/**
 * This component caches the latest HTML version of the home page's content.
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

@Named("homePage")
@javax.enterprise.context.ApplicationScoped
@Slf4j
public class HomePage {

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private CommonMarkRenderer renderer;

    private String html;

    /**
     * Returns the rendered, sanitised HTML for the home page content set by admin.
     * @return
     */
    public String getHtml() {
        if (html == null) {
            updateHtml();
        }
        return html;
    }

    @Observer(HomeContentChangedEvent.EVENT_NAME)
    /**
     * Event handler to update the cached HTML based on the latest CommonMark home content.
     */
    public void updateHtml() {
        String text = applicationConfiguration.getHomeContent();
        if (text == null) {
            html = "";
        } else {
            html = renderer.renderToHtmlSafe(text);
        }
    }

}
