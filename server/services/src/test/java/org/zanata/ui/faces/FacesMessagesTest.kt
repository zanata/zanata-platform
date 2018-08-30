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
package org.zanata.ui.faces

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.KotlinAssertions.assertThat
import org.jglue.cdiunit.AdditionalClasses
import org.jglue.cdiunit.InRequestScope
import org.jglue.cdiunit.ProducesAlternative
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.zanata.cdi.WithActiveWindow
import org.zanata.cdi.WithActiveWindowInterceptor
import org.zanata.i18n.Messages
import org.zanata.test.CdiUnitRunner
import java.util.Collections.emptyIterator
import javax.enterprise.context.RequestScoped
import javax.enterprise.inject.Produces
import javax.faces.application.FacesMessage
import javax.faces.application.FacesMessage.SEVERITY_INFO
import javax.faces.component.UIViewRoot
import javax.faces.context.FacesContext
import javax.inject.Inject

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@RunWith(CdiUnitRunner::class)
@AdditionalClasses(WithActiveWindowInterceptor::class)
@InRequestScope
@WithActiveWindow("34")
@SupportDeltaspikeCore
class FacesMessagesTest {

    private val testMessage = "test message"
    private val message = ArgumentCaptor.forClass(FacesMessage::class.java)

    @Inject
    private lateinit var facesMessages: FacesMessages

    @Produces
    @Mock
    private lateinit var msgs: Messages

    @Produces
    @Mock
    @ProducesAlternative
    @RequestScoped
    private lateinit var facesContext: FacesContext
    @Produces
    @Mock
    private lateinit var uiViewRoot: UIViewRoot

    @Before
    fun setup() {
        whenever(facesContext.viewRoot) doReturn uiViewRoot
        whenever(uiViewRoot.facetsAndChildren) doReturn emptyIterator()
    }

    @Test
    fun addToControl() {
        whenever(uiViewRoot.id) doReturn "id"
        whenever(uiViewRoot.clientId) doReturn "clientId"
        facesMessages.addToControl("id", testMessage)
        facesMessages.beforeRenderResponse()

        verify(facesContext).addMessage(eq("clientId"), message.capture())
        assertThat(message.value.summary).isEqualTo(testMessage)
        assertThat(message.value.severity).isEqualTo(SEVERITY_INFO)
    }

    @Test
    fun addGlobalString() {
        facesMessages.addGlobal(testMessage)
        facesMessages.beforeRenderResponse()
        verify(facesContext).addMessage(isNull(), message.capture())
        assertThat(message.value.summary).isEqualTo(testMessage)
        assertThat(message.value.severity).isEqualTo(SEVERITY_INFO)
    }

    @Test
    fun addGlobalFacesMessage() {
        val msg = FacesMessage(testMessage)
        facesMessages.addGlobal(msg)
        facesMessages.beforeRenderResponse()
        verify(facesContext).addMessage(isNull(), message.capture())
        assertThat(message.value).isSameAs(msg)
        assertThat(message.value.severity).isEqualTo(SEVERITY_INFO)
    }

}
