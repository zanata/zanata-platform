package org.zanata.client.commands.pull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.DummyResponse;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PublicanPullCommand;
import org.zanata.client.commands.PublicanPullOptions;
import org.zanata.client.commands.PublicanPullOptionsImpl;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PublicanPullCommandTest {
    @Mock
    private ISourceDocResource mockSourceDocResource;

    @Mock
    private ITranslatedDocResource mockTranslationResources;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void publicanPullPo() throws Exception {
        publicanPull(false, false);
    }

    @Test
    public void publicanPullPotAndPo() throws Exception {
        publicanPull(true, false);
    }

    @Test
    public void publicanPullPotAndPoWithLocaleMapping() throws Exception {
        publicanPull(true, true);
    }

    @SuppressWarnings("deprecation")
    private void publicanPull(boolean exportPot, boolean mapLocale)
            throws Exception {
        PublicanPullOptions opts = new PublicanPullOptionsImpl();
        String projectSlug = "project";
        opts.setProj(projectSlug);
        String versionSlug = "1.0";
        opts.setProjectVersion(versionSlug);
        opts.setDstDir(new File("target/test-output/test2"));
        opts.setExportPot(exportPot);
        opts.setProjectConfig(new File("src/test/resources/test2/zanata.xml"));
        OptionsUtil.applyConfigFiles(opts);
        if (mapLocale) {
            LocaleList locales = new LocaleList();
            locales.add(new LocaleMapping("ja", "ja-JP"));
            opts.setLocaleMapList(locales);
        }

        List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
        resourceMetaList.add(new ResourceMeta("RPM"));
        resourceMetaList.add(new ResourceMeta("sub/RPM"));

        when(mockSourceDocResource.get(null)).thenReturn(
                new DummyResponse<List<ResourceMeta>>(Status.OK,
                        resourceMetaList));

        Resource rpmResource = new Resource("RPM");

        StringSet ext = new StringSet("comment;gettext");
        when(
                mockSourceDocResource.getResource(
                        RestUtil.convertToDocumentURIId(rpmResource.getName()),
                        ext)).thenReturn(
                new DummyResponse<Resource>(Status.OK, rpmResource));

        Resource subRpmResource = new Resource("sub/RPM");
        when(
                mockSourceDocResource.getResource(RestUtil
                        .convertToDocumentURIId(subRpmResource.getName()), ext))
                .thenReturn(
                        new DummyResponse<Resource>(Status.OK, subRpmResource));

        LocaleId expectedLocale;
        if (mapLocale)
            expectedLocale = new LocaleId("ja");
        else
            expectedLocale = new LocaleId("ja-JP");
        TranslationsResource rpmTransJa = new TranslationsResource();
        mockExpectGetTranslationsAndReturnResponse("RPM", expectedLocale,
                rpmTransJa);
        mockExpectGetTranslationsAndReturnResponse("sub/RPM", expectedLocale,
                null);
        ZanataProxyFactory mockRequestFactory = mock(ZanataProxyFactory.class);

        ZanataCommand cmd =
                new PublicanPullCommand(opts, mockRequestFactory,
                        mockSourceDocResource, mockTranslationResources,
                        new URI("http://example.com/"));
        cmd.run();
    }

    private void mockExpectGetTranslationsAndReturnResponse(String id,
            LocaleId locale, TranslationsResource entity) {
        String docUri = RestUtil.convertToDocumentURIId(id);
        StringSet ext = new StringSet("comment;gettext");
        if (entity != null) {
            when(mockTranslationResources.getTranslations(docUri, locale, ext))
                    .thenReturn(
                            new DummyResponse<TranslationsResource>(Status.OK,
                                    entity));
        } else {
            when(mockTranslationResources.getTranslations(docUri, locale, ext))
                    .thenReturn(
                            new DummyResponse<TranslationsResource>(
                                    Status.NOT_FOUND, entity));
        }
    }

}
