package org.zanata.client.commands.push;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.DummyResponse;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PublicanPushCommand;
import org.zanata.client.commands.PublicanPushOptionsImpl;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PublicanPushCommandTest {
    @Mock
    ISourceDocResource mockSourceDocResource;

    @Mock
    ITranslatedDocResource mockTranslationResources;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void publicanPushPot() throws Exception {
        publicanPush(false, false);
    }

    @Test
    public void publicanPushPotAndPo() throws Exception {
        publicanPush(true, false);
    }

    @Test
    public void publicanPushPotAndPoWithLocaleMapping() throws Exception {
        publicanPush(true, true);
    }

    @SuppressWarnings("deprecation")
    private void publicanPush(boolean importPo, boolean mapLocale)
            throws Exception {
        PublicanPushOptionsImpl opts = new PublicanPushOptionsImpl();
        opts.setInteractiveMode(false);
        String projectSlug = "project";
        opts.setProj(projectSlug);
        String versionSlug = "1.0";
        opts.setProjectVersion(versionSlug);
        opts.setSrcDir(new File("src/test/resources/test1"));
        opts.setImportPo(importPo);
        OptionsUtil.applyConfigFiles(opts);
        if (mapLocale) {
            LocaleList locales = new LocaleList();
            locales.add(new LocaleMapping("ja", "ja-JP"));
            opts.setLocaleMapList(locales);
        }

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
        when(
                mockSourceDocResource.putResource(eq("RPM"),
                        (Resource) isNotNull(), eq(extensionSet), eq(true)))
                .thenReturn(okResponse);
        when(
                mockSourceDocResource.putResource(eq("sub,RPM"),
                        (Resource) isNotNull(), eq(extensionSet), eq(true)))
                .thenReturn(okResponse);

        if (importPo) {
            LocaleId expectedLocale;
            if (mapLocale)
                expectedLocale = new LocaleId("ja");
            else
                expectedLocale = new LocaleId("ja-JP");
            when(
                    mockTranslationResources.putTranslations(eq("RPM"),
                            eq(expectedLocale),
                            (TranslationsResource) isNotNull(),
                            eq(extensionSet), eq("auto"))).thenReturn(
                    okResponse);
        }
        ZanataProxyFactory mockRequestFactory = mock(ZanataProxyFactory.class);

        ZanataCommand cmd =
                new PublicanPushCommand(opts, mockRequestFactory,
                        mockSourceDocResource, mockTranslationResources,
                        new URI("http://example.com/"));
        cmd.runWithActions();
    }

}
