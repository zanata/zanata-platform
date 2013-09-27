package org.zanata.client.commands.push;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.mockito.Mock;
import org.zanata.client.commands.DummyResponse;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.AsynchronousProcessResource;
import org.zanata.rest.service.CopyTransResource;

public class PushCommandTest {
    @Mock
    ZanataProxyFactory mockRequestFactory;
    @Mock
    ISourceDocResource mockSourceDocResource;
    @Mock
    ITranslatedDocResource mockTranslationResources;
    @Mock
    CopyTransResource mockCopyTransResource;
    @Mock
    AsynchronousProcessResource mockAsynchronousProcessResource;

    public PushCommandTest() throws Exception {
        initMocks(this);
    }

    @Test
    public void pushSrc() throws Exception {
        push(false, false);
    }

    @Test
    public void pushSrcAndTarget() throws Exception {
        push(true, false);
    }

    @Test
    public void pushSrcAndTargetWithLocaleMapping() throws Exception {
        push(true, true);
    }

    @Test
    public void testSplitTranslationResource() throws Exception {
        int batchSize = 100;
        int listSize = 500;

        checkSplitResult(listSize, batchSize);

        batchSize = 50;
        listSize = 500;

        checkSplitResult(listSize, batchSize);
    }

    @Test
    public void testSplitTranslationResourceWithMod() throws Exception {
        int batchSize = 100;
        int listSize = 505;

        checkSplitResult(listSize, batchSize);

        batchSize = 100;
        listSize = 510;

        checkSplitResult(listSize, batchSize);
    }

    private void checkSplitResult(int listSize, int batchSize) throws Exception {
        PushCommand cmd = generatePushCommand(true, true);
        TranslationsResource transRes = new TranslationsResource();
        for (int i = 0; i < listSize; i++) {
            transRes.getTextFlowTargets().add(
                    new TextFlowTarget(String.valueOf(i)));
        }

        List<TranslationsResource> list =
                cmd.splitIntoBatch(transRes, batchSize);

        int expectListSize = listSize / batchSize;
        if (listSize % batchSize != 0) {
            expectListSize = expectListSize + 1;
        }

        int expectLastTftSize = listSize % batchSize;
        if (expectLastTftSize == 0) {
            expectLastTftSize = batchSize;
        }

        assertEquals(list.size(), expectListSize);
        assertEquals(list.get(0).getTextFlowTargets().size(), batchSize);
        assertEquals(list.get(list.size() - 1).getTextFlowTargets().size(),
                expectLastTftSize);
    }

    private PushCommand
            generatePushCommand(boolean pushTrans, boolean mapLocale)
                    throws Exception {

        PushOptionsImpl opts = new PushOptionsImpl();
        opts.setInteractiveMode(false);
        String projectSlug = "project";
        opts.setProj(projectSlug);
        String versionSlug = "1.0";
        opts.setProjectVersion(versionSlug);
        opts.setSrcDir(new File("src/test/resources/test1/pot"));
        if (pushTrans) {
            opts.setPushType("both");
        } else {
            opts.setPushType("source");
        }
        opts.setTransDir(new File("src/test/resources/test1"));
        opts.setProjectType("podir");
        // opts.setNoCopyTrans(false);
        opts.setCopyTrans(true);
        opts.setIncludes("**/*.pot");
        opts.setExcludes("");
        opts.setSourceLang("en-US");
        opts.setMergeType("auto");
        LocaleList locales = new LocaleList();
        if (mapLocale) {
            locales.add(new LocaleMapping("ja", "ja-JP"));
        } else {
            locales.add(new LocaleMapping("ja-JP"));
        }
        opts.setLocaleMapList(locales);
        OptionsUtil.applyConfigFiles(opts);

        when(mockRequestFactory.getCopyTransResource()).thenReturn(
                mockCopyTransResource);
        when(mockRequestFactory.getAsynchronousProcessResource()).thenReturn(
                mockAsynchronousProcessResource);

        return new PushCommand(opts, mockRequestFactory, mockSourceDocResource,
                mockTranslationResources, new URI("http://example.com/"));
    }

    private void push(boolean pushTrans, boolean mapLocale) throws Exception {
        List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
        resourceMetaList.add(new ResourceMeta("obsolete"));
        resourceMetaList.add(new ResourceMeta("RPM"));
        when(mockSourceDocResource.get(null)).thenReturn(
                new DummyResponse<List<ResourceMeta>>(Status.OK,
                        resourceMetaList));

        final ClientResponse<String> okResponse =
                new DummyResponse<String>(Status.OK, null);
        when(mockSourceDocResource.deleteResource("obsolete")).thenReturn(
                okResponse);
        StringSet extensionSet = new StringSet("gettext;comment");
        // TODO These calls now use a false copyTrans value (2.0) but they
        // invoke the copy Trans resource. Still need to add Copy Trans resource
        // expectations
        // when( mockSourceDocResource.putResource(eq("RPM"), (Resource)
        // notNull(), eq(extensionSet), eq(false)) ).thenReturn(okResponse);
        // when( mockSourceDocResource.putResource(eq("sub,RPM"), (Resource)
        // notNull(), eq(extensionSet), eq(false))).thenReturn(okResponse);
        ProcessStatus mockStatus = new ProcessStatus();
        mockStatus.setStatusCode(ProcessStatus.ProcessStatusCode.Finished);
        mockStatus.setMessages(new ArrayList<String>());
        when(
                mockAsynchronousProcessResource.startSourceDocCreationOrUpdate(
                        eq("RPM"), anyString(), anyString(),
                        (Resource) notNull(), eq(extensionSet), eq(false)))
                .thenReturn(mockStatus);
        when(
                mockAsynchronousProcessResource.startSourceDocCreationOrUpdate(
                        eq("sub,RPM"), anyString(), anyString(),
                        (Resource) notNull(), eq(extensionSet), eq(false)))
                .thenReturn(mockStatus);
        when(mockAsynchronousProcessResource.getProcessStatus(anyString()))
                .thenReturn(mockStatus);

        CopyTransStatus mockCopyTransStatus = new CopyTransStatus();
        mockCopyTransStatus.setInProgress(false);
        mockCopyTransStatus.setPercentageComplete(100);
        when(
                mockCopyTransResource.getCopyTransStatus(anyString(),
                        anyString(), anyString())).thenReturn(
                mockCopyTransStatus);

        if (pushTrans) {
            LocaleId expectedLocale;
            if (mapLocale) {
                expectedLocale = new LocaleId("ja");
            } else {
                expectedLocale = new LocaleId("ja-JP");
            }
            when(
                    mockAsynchronousProcessResource
                            .startTranslatedDocCreationOrUpdate(eq("RPM"),
                                    anyString(), anyString(),
                                    eq(expectedLocale),
                                    (TranslationsResource) notNull(),
                                    eq(extensionSet), eq("auto"))).thenReturn(
                    mockStatus);
            // when(mockTranslationResources.putTranslations(eq("RPM"),
            // eq(expectedLocale), (TranslationsResource) notNull(),
            // eq(extensionSet), eq("auto")))
            // .thenReturn(okResponse);
        }
        ZanataCommand cmd = generatePushCommand(pushTrans, mapLocale);
        cmd.run();
    }

}
