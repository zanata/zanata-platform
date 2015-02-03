/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.ui.faces;

import org.zanata.util.ServiceLocator;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import static javax.faces.event.PhaseId.ANY_PHASE;
import static javax.faces.event.PhaseId.RENDER_RESPONSE;

/**
 * Phase listener that transfers messages from our own custom FacesMessages
 * bean. NB: May be removed after CDI migration if there is a suitable
 * replacement. (See DeltaSpike for possible replacements.)
 *
 * @see {@link org.jboss.seam.jsf.SeamPhaseListener}
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class FacesMessagesPhaseListener implements PhaseListener {
    @Override
    public void afterPhase(PhaseEvent event) {
        // Nothing to do here
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == RENDER_RESPONSE) {
            if (!event.getFacesContext().getResponseComplete()) {
                ServiceLocator.instance().getInstance(FacesMessages.class)
                        .beforeRenderResponse();
            }
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return ANY_PHASE;
    }
}
