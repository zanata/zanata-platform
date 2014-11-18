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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.rules.ExternalResource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.pull.RawPullCommand;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.AsyncProcessClient;
import org.zanata.rest.client.CopyTransClient;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.client.SourceDocResourceClient;
import org.zanata.rest.client.TransDocResourceClient;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.FileResource;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Test rule to set up push and/or pull command(s) which will interact with a
 * mockito mocked REST clients.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockitoServerRule extends ExternalResource {
    // async process statuses
    private String mockProcessId = "MockServerRuleProcess";
    private ProcessStatus finished = new ProcessStatus();
    private ProcessStatus running = new ProcessStatus();
    private PullOptionsImpl pullOpts;

    private PushOptionsImpl pushOpts;
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
    private ClientResponse transResourceResponse;
    @Captor
    private ArgumentCaptor<DocumentFileUploadForm> uploadFormCaptor;
    @Mock
    private ClientResponse downloadSourceResponse;
    @Mock
    private ClientResponse downloadTransResponse;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private CopyTransClient copyTransClient;
    @Mock
    private AsyncProcessClient asyncClient;
    @Mock
    private SourceDocResourceClient sourceDocClient;
    @Mock
    private TransDocResourceClient transDocClient;
    @Mock
    private FileResourceClient fileResourceClient;

    public MockitoServerRule() {
        MockitoAnnotations.initMocks(this);
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
     * @see MockitoServerRule#verifyPushSource()
     * @see MockitoServerRule#verifyPushTranslation()
     * @return push command
     */
    public PushCommand createPushCommand() {
        when(clientFactory.getSourceDocResourceClient(pushOpts.getProj(),
                pullOpts.getProjectVersion())).thenReturn(sourceDocClient);
        when(sourceDocClient.getResourceMeta(null)).thenReturn(
                Collections.<ResourceMeta>emptyList());
        // this assumes async push is always success
        when(
                asyncClient.startSourceDocCreationOrUpdate(
                        anyString(),
                        eq(pushOpts.getProj()), eq(pushOpts.getProjectVersion()),
                        any(Resource.class), anySetOf(String.class),
                        eq(false)))
                .thenReturn(running);
        when(
                asyncClient.startTranslatedDocCreationOrUpdate(
                        docIdCaptor.capture(), eq(pushOpts.getProj()),
                        eq(pushOpts.getProjectVersion()), localeIdCaptor.capture(),
                        transResourceCaptor.capture(),
                        extensionCaptor.capture(), eq(pushOpts.getMergeType())))
                .thenReturn(running);
        when(asyncClient.getProcessStatus(mockProcessId))
                .thenReturn(finished);
        return new PushCommand(pushOpts,
                copyTransClient,
                asyncClient, clientFactory);
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
        verify(asyncClient).startSourceDocCreationOrUpdate(
                docIdCaptor.capture(), eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()), resourceCaptor.capture(),
                extensionCaptor.capture(), eq(false));
    }

    public void verifyPushTranslation() {
        verify(asyncClient).startTranslatedDocCreationOrUpdate(
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
        when(clientFactory.getSourceDocResourceClient(anyString(), anyString()))
                .thenReturn(sourceDocClient);
        // return provided remote doc meta list
        when(sourceDocClient.getResourceMeta(null)).thenReturn(remoteDocList);
        // return provided server resource
        when(sourceDocClient.getResource(anyString(), anySetOf(String.class)))
                .thenReturn(resourceOnServer);
        when(
                clientFactory.getTransDocResourceClient(pullOpts.getProj(),
                        pullOpts.getProjectVersion())).thenReturn(
                transDocClient);
        // return provided server translation
        when(
                transDocClient.getTranslations(anyString(),
                        any(LocaleId.class), anySetOf(String.class),
                        eq(getPullOpts().getCreateSkeletons()), anyString()))
                .thenReturn(transResourceResponse);
        when(transResourceResponse.getStatus()).thenReturn(200);
        when(transResourceResponse.getHeaders()).thenReturn(
                new MultivaluedMapImpl());
        when(transResourceResponse.getEntity(TranslationsResource.class))
                .thenReturn(transResourceOnServer);
        return new PullCommand(pullOpts, clientFactory);
    }

    /**
     * Creates a raw file push command that will interact with mock REST proxy factory.
     * File service resource is stubbed to always return successful result.
     *
     * @see MockitoServerRule#verifyPushRawFileSource(int)
     * @see MockitoServerRule#verifyPushRawFileTranslation(int)
     * @return raw push command
     */
    public RawPushCommand createRawPushCommand() {
        when(clientFactory.getFileResourceClient()).thenReturn(
                fileResourceClient);
        List<String> fileTypes =
                ProjectType.getSupportedSourceFileTypes(ProjectType.File);
        when(fileResourceClient.acceptedFileTypes())
                .thenReturn(new StringSet(Joiner.on(";").join(fileTypes)));
        ChunkUploadResponse uploadResponse =
                new ChunkUploadResponse(1L, 1, false, "Upload successful");
        // push source
        when(fileResourceClient.uploadSourceFile(eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()), anyString(), any(
                        DocumentFileUploadForm.class))).thenReturn(uploadResponse);
        // push translation
        when(
                fileResourceClient.uploadTranslationFile(eq(pushOpts.getProj()),
                        eq(pushOpts.getProjectVersion()), anyString(),
                        anyString(), anyString(),
                        any(DocumentFileUploadForm.class))).thenReturn(
                uploadResponse);
        return new RawPushCommand(pushOpts, clientFactory);
    }

    public void verifyPushRawFileSource(int numOfSource) {
        verify(fileResourceClient, times(numOfSource)).uploadSourceFile(
                eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()), docIdCaptor.capture(),
                uploadFormCaptor.capture());
    }

    public void verifyPushRawFileTranslation(int numOfTrans) {
        verify(fileResourceClient, times(numOfTrans)).uploadTranslationFile(
                eq(pushOpts.getProj()),
                eq(pushOpts.getProjectVersion()),
                anyString(), docIdCaptor.capture(),
                anyString(), uploadFormCaptor.capture());
    }

    /**
     * Creates a raw pull command that will interact with mock REST proxy factory
     * and get back provided response.
     *
     * @param remoteDocList
     *            stubbed server response that represents source doc meta list
     * @param sourceFileStream
     *            stubbed server response that represents source doc stream
     * @param transFileStream
     *            stubbed server response that represents translation doc stream
     * @return raw pull command
     */
    public RawPullCommand createRawPullCommand(
            List<ResourceMeta> remoteDocList,
            InputStream sourceFileStream, InputStream transFileStream) {
        when(
                clientFactory.getSourceDocResourceClient(pullOpts.getProj(),
                        pullOpts.getProjectVersion())).thenReturn(
                sourceDocClient);
        when(clientFactory.getFileResourceClient()).thenReturn(fileResourceClient);
        // return provided remote doc meta list
        when(sourceDocClient.getResourceMeta(null)).thenReturn(remoteDocList);
        // return provided source stream
        when(fileResourceClient.downloadSourceFile(
                eq(pullOpts.getProj()), eq(pullOpts.getProjectVersion()),
                eq(FileResource.FILETYPE_RAW_SOURCE_DOCUMENT),
                anyString())).thenReturn(downloadSourceResponse);
        when(downloadSourceResponse.getClientResponseStatus()).thenReturn(
                ClientResponse.Status.OK);
        when(downloadSourceResponse.getStatus()).thenReturn(200);
        when(downloadSourceResponse.getEntity(InputStream.class))
                .thenReturn(sourceFileStream);
        // return provide translation stream
        when(fileResourceClient.downloadTranslationFile(eq(pullOpts.getProj()),
                eq(pullOpts.getProjectVersion()), anyString(), anyString(),
                anyString())).thenReturn(downloadTransResponse);
        when(downloadTransResponse.getStatus()).thenReturn(200);
        when(downloadTransResponse.getClientResponseStatus()).thenReturn(
                ClientResponse.Status.OK);
        when(downloadTransResponse.getEntity(InputStream.class))
                .thenReturn(transFileStream);
        return new RawPullCommand(pullOpts, fileResourceClient, clientFactory);
    }
}
