package org.zanata.client.commands.push;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.zanata.client.TestUtils.fileFromClasspath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.zanata.client.TestUtils;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.AsyncProcessClient;
import org.zanata.rest.client.CopyTransClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.client.SourceDocResourceClient;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PushCommandTest {
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private SourceDocResourceClient sourceDocResourceClient;
    @Mock
    private AsyncProcessClient asyncProcessClient;
    @Mock
    private CopyTransClient copyTransClient;

    @Before
    public void setUp() {
        initMocks(this);
        when(clientFactory.getSourceDocResourceClient(anyString(), anyString()))
                .thenReturn(sourceDocResourceClient);
        when(clientFactory.getAsyncProcessClient()).thenReturn(
                asyncProcessClient);
        when(clientFactory.getCopyTransClient()).thenReturn(copyTransClient);
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
        opts.setSrcDir(fileFromClasspath("test1/pot"));
        if (pushTrans) {
            opts.setPushType("both");
        } else {
            opts.setPushType("source");
        }
        opts.setTransDir(fileFromClasspath("test1"));
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

        return new PushCommand(opts,
                clientFactory.getCopyTransClient(),
                clientFactory.getAsyncProcessClient(), clientFactory);
    }

    private void push(boolean pushTrans, boolean mapLocale) throws Exception {
        List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
        resourceMetaList.add(new ResourceMeta("obsolete"));
        resourceMetaList.add(new ResourceMeta("RPM"));
        when(sourceDocResourceClient.getResourceMeta(null)).thenReturn(
                        resourceMetaList);
        when(sourceDocResourceClient.deleteResource("obsolete")).thenReturn(
                null);
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
                asyncProcessClient.startSourceDocCreationOrUpdate(
                        eq("RPM"), anyString(), anyString(),
                        any(Resource.class), eq(extensionSet), eq(false)))
                .thenReturn(mockStatus);
        when(
                asyncProcessClient.startSourceDocCreationOrUpdate(
                        eq("sub,RPM"), anyString(), anyString(),
                        any(Resource.class), eq(extensionSet), eq(false)))
                .thenReturn(mockStatus);
        when(asyncProcessClient.getProcessStatus(anyString()))
                .thenReturn(mockStatus);

        CopyTransStatus mockCopyTransStatus = new CopyTransStatus();
        mockCopyTransStatus.setInProgress(false);
        mockCopyTransStatus.setPercentageComplete(100);
        when(
                copyTransClient.getCopyTransStatus(anyString(),
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
                    asyncProcessClient
                            .startTranslatedDocCreationOrUpdate(eq("RPM"),
                                    anyString(), anyString(),
                                    eq(expectedLocale),
                                    any(TranslationsResource.class),
                                    eq(extensionSet), eq("auto"))).thenReturn(
                    mockStatus);
            // when(mockTranslationResources.putTranslations(eq("RPM"),
            // eq(expectedLocale), (TranslationsResource) notNull(),
            // eq(extensionSet), eq("auto")))
            // .thenReturn(okResponse);
        }
        ZanataCommand cmd = generatePushCommand(pushTrans, mapLocale);
        cmd.runWithActions();
    }

}
