/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.client;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;

import org.jboss.resteasy.client.ClientResponse;
import org.junit.rules.ExternalResource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.LocaleList;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.IAsynchronousProcessResource;
import org.zanata.rest.client.ICopyTransResource;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.base.Throwables;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test rule to set up push and/or pull command(s) which will interact with a
 * mock REST proxy factory and mocked REST resources.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockServerRule extends ExternalResource {
    private URI uri;
    @Mock
    private IAsynchronousProcessResource asyncResource;
    @Mock
    private ICopyTransResource copyTransResource;
    @Mock
    private ZanataProxyFactory factory;
    @Mock
    private ISourceDocResource sourceDocResource;
    @Mock
    private ITranslatedDocResource transDocResource;
    private PushOptionsImpl pushOpts;
    @Mock
    private ClientResponse<List<ResourceMeta>> remoteDocListResponse;
    @Captor
    private ArgumentCaptor<Resource> resourceCaptor;
    @Captor
    private ArgumentCaptor<Set<String>> extensionCaptor;
    @Captor
    private ArgumentCaptor<String> docIdCaptor;
    @Captor
    private ArgumentCaptor<LocaleId> localeIdCaptor;
    @Captor
    private ArgumentCaptor<TranslationsResource> transResourceCaptor;
    @Mock
    private ClientResponse<Resource> resourceResponse;
    @Mock
    private ClientResponse<TranslationsResource> transResourceResponse;

    // async process statuses
    private String mockProcessId = "MockServerRuleProcess";
    private ProcessStatus finished = new ProcessStatus();
    private ProcessStatus running = new ProcessStatus();
    private PullOptionsImpl pullOpts;

    public MockServerRule() {
        MockitoAnnotations.initMocks(this);
        when(factory.getCopyTransResource()).thenReturn(copyTransResource);
        when(factory.getAsynchronousProcessResource()).thenReturn(asyncResource);
        // async process statuses
        running.setUrl(mockProcessId);
        running.setStatusCode(ProcessStatus.ProcessStatusCode.Running);
        finished.setUrl(mockProcessId);
        finished.setStatusCode(ProcessStatus.ProcessStatusCode.Finished);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        pushOpts = new PushOptionsImpl();
        setCommonOpts(pushOpts);
        pushOpts.setCopyTrans(false);

        pullOpts = new PullOptionsImpl();
        setCommonOpts(pullOpts);
    }

    private static void setCommonOpts(ConfigurableProjectOptions opts) {
        opts.setUsername("admin");
        opts.setKey("abcde");
        opts.setLocaleMapList(new LocaleList());
        opts.setInteractiveMode(false);
        setUrl(opts);
        opts.setProj("sample-project");
        opts.setProjectVersion("master");
    }

    private static URI setUrl(ConfigurableProjectOptions opts) {
        try {
            URI uri = new URI("http://localhost:8888/zanata");
            opts.setUrl(uri.toURL());
            return uri;
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Creates a push command that will interact with mock REST proxy factory.
     * For async push source and translations the responses are assumed always
     * successful. You should verify push afterwards and make assertion on
     * all/some of the captor captured values.
     *
     * @see MockServerRule#verifyPushSource()
     * @see MockServerRule#verifyPushTranslation()
     * @return push command
     */
    public PushCommand createPushCommand() {
        when(sourceDocResource.get(null)).thenReturn(remoteDocListResponse);
        // this assumes no obsolete documents on server
        when(remoteDocListResponse.getStatus()).thenReturn(200);
        when(remoteDocListResponse.getEntity()).thenReturn(
                Collections.<ResourceMeta> emptyList());
        // this assumes async push is always success
        when(
                asyncResource.startSourceDocCreationOrUpdate(
                        anyString(),
                        eq(pushOpts.getProj()), eq(pushOpts.getProjectVersion()),
                        any(Resource.class), anySetOf(String.class),
                        eq(false)))
                .thenReturn(running);
        when(
                asyncResource.startTranslatedDocCreationOrUpdate(
                        docIdCaptor.capture(), eq(pushOpts.getProj()),
                        eq(pushOpts.getProjectVersion()), localeIdCaptor.capture(),
                        transResourceCaptor.capture(),
                        extensionCaptor.capture(), eq(pushOpts.getMergeType())))
                .thenReturn(running);
        when(asyncResource.getProcessStatus(mockProcessId))
                .thenReturn(finished);
        return new PushCommand(pushOpts, factory, sourceDocResource, transDocResource, uri);
    }

    public PushOptionsImpl getPushOpts() {
        return pushOpts;
    }

    public ArgumentCaptor<Resource> getResourceCaptor() {
        return resourceCaptor;
    }

    public ArgumentCaptor<Set<String>> getExtensionCaptor() {
        return extensionCaptor;
    }

    public ArgumentCaptor<String> getDocIdCaptor() {
        return docIdCaptor;
    }

    public ArgumentCaptor<LocaleId> getLocaleIdCaptor() {
        return localeIdCaptor;
    }

    public ArgumentCaptor<TranslationsResource> getTransResourceCaptor() {
        return transResourceCaptor;
    }

    public void verifyPushSource() {
        verify(asyncResource).startSourceDocCreationOrUpdate(
                docIdCaptor.capture(), eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()), resourceCaptor.capture(),
                extensionCaptor.capture(), eq(false));
    }

    public void verifyPushTranslation() {
        verify(asyncResource).startTranslatedDocCreationOrUpdate(
                docIdCaptor.capture(), eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()), localeIdCaptor.capture(),
                transResourceCaptor.capture(), extensionCaptor.capture(),
                eq(pushOpts.getMergeType()));
    }

    public PullOptionsImpl getPullOpts() {
        return pullOpts;
    }

    /**
     * Creates a pull command that will interact with mock REST proxy factory
     * and get back provided response.
     *
     * @param remoteDocList
     *            stubbed server response that represents source doc meta list
     * @param resourceOnServer
     *            stubbed server response that represents source on server
     * @param transResourceOnServer
     *            stubbed server response that represents translation on server
     * @return pull command
     */
    public PullCommand createPullCommand(List<ResourceMeta> remoteDocList,
            Resource resourceOnServer,
            TranslationsResource transResourceOnServer) {
        // return provided remote doc meta list
        when(sourceDocResource.get(null)).thenReturn(remoteDocListResponse);
        when(remoteDocListResponse.getEntity()).thenReturn(remoteDocList);
        // return provided server resource
        when(sourceDocResource.getResource(anyString(), anySetOf(String.class)))
                .thenReturn(resourceResponse);
        when(resourceResponse.getStatus()).thenReturn(200);
        when(resourceResponse.getEntity()).thenReturn(resourceOnServer);
        // return provided server translation
        when(
                transDocResource.getTranslations(anyString(),
                        any(LocaleId.class), anySetOf(String.class),
                        eq(getPullOpts().getCreateSkeletons()), anyString()))
                .thenReturn(transResourceResponse);
        when(transResourceResponse.getStatus()).thenReturn(200);
        when(transResourceResponse.getResponseHeaders()).thenReturn(
                new MultivaluedHashMap<String, String>());
        when(transResourceResponse.getEntity()).thenReturn(
                transResourceOnServer);
        return new PullCommand(pullOpts, factory, sourceDocResource,
                transDocResource, uri);
    }
}
