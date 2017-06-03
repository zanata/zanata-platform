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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.ProducesAlternative;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.zanata.cdi.WithActiveWindow;
import org.zanata.cdi.WithActiveWindowInterceptor;
import org.zanata.i18n.Messages;
import org.zanata.test.CdiUnitRunner;

import static java.util.Collections.emptyIterator;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ WithActiveWindowInterceptor.class})
@InRequestScope
@WithActiveWindow("34")
@SupportDeltaspikeCore
public class FacesMessagesTest {

    private String testMessage = "test message";
    private ArgumentCaptor<FacesMessage> message =
            ArgumentCaptor.forClass(FacesMessage.class);

    @Inject
    private FacesMessages facesMessages;

    @Produces @Mock
    private Messages msgs;

    @Produces @Mock @ProducesAlternative
    @RequestScoped
    private FacesContext facesContext;
    @Produces @Mock
    private UIViewRoot uiViewRoot;

    @Before
    public void setup() {
        when(facesContext.getViewRoot()).thenReturn(uiViewRoot);
        when(uiViewRoot.getFacetsAndChildren()).thenReturn(emptyIterator());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addToControl() throws Exception {
        when(uiViewRoot.getId()).thenReturn("id");
        when(uiViewRoot.getClientId()).thenReturn("clientId");
        facesMessages.addToControl("id", testMessage);
        facesMessages.beforeRenderResponse();

        verify(facesContext).addMessage(eq("clientId"), message.capture());
        assertThat(message.getValue().getSummary()).isEqualTo(testMessage);
        assertThat(message.getValue().getSeverity()).isEqualTo(
                SEVERITY_INFO);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addGlobalString() throws Exception {
        facesMessages.addGlobal(testMessage);
        facesMessages.beforeRenderResponse();
        verify(facesContext).addMessage(eq(null), message.capture());
        assertThat(message.getValue().getSummary()).isEqualTo(testMessage);
        assertThat(message.getValue().getSeverity()).isEqualTo(
                SEVERITY_INFO);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addGlobalFacesMessage() throws Exception {
        FacesMessage msg = new FacesMessage(testMessage);
        facesMessages.addGlobal(msg);
        facesMessages.beforeRenderResponse();
        verify(facesContext).addMessage(eq(null), message.capture());
        assertThat(message.getValue()).isSameAs(msg);
        assertThat(message.getValue().getSeverity()).isEqualTo(
                SEVERITY_INFO);
    }

}
