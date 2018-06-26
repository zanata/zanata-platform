
package org.zanata.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.zanata.common.LocaleId.EN_US;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.MachineTranslationService;

public class MachineTranslationResourceTest {

    private MachineTranslationResource resource;

    @Mock
    private DocumentDAO documentDAO;
    @Mock
    private TextFlowDAO textFlowDAO;
    @Mock
    private MachineTranslationService machineTranslationService;
    @Mock private ActiveProjectVersionAndLocaleValidator
            activeProjectVersionAndLocaleValidator;
    @Mock private ZanataIdentity identity;
    @Mock private MachineTranslationsManager machineTranslationManager;
    @Mock private UriInfo uri;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        resource = new MachineTranslationResource(documentDAO, textFlowDAO,
                machineTranslationService,
                activeProjectVersionAndLocaleValidator, identity, machineTranslationManager);
    }

    @Test
    public void exceptionWhenDocumentIsNotFound() {
        assertThatThrownBy(() -> resource.getMachineTranslationSuggestion("prj",
                "ver", "docId", "resId", "zh"))
                        .isInstanceOf(NoSuchEntityException.class);
    }

    @Test
    public void exceptionWhenTextFlowIsNotFound() {
        when(documentDAO.getByProjectIterationAndDocId("proj", "ver", "docId"))
                .thenReturn(new HDocument());
        assertThatThrownBy(() -> resource.getMachineTranslationSuggestion(
                "proj", "ver", "docId", "resId", "zh"))
                        .isInstanceOf(NoSuchEntityException.class);
    }

    @Test
    public void willGetSuggestionFromMT() {
        HDocument document = new HDocument();
        document.setLocale(new HLocale(LocaleId.EN_US));
        when(documentDAO.getByProjectIterationAndDocId("proj", "ver", "docId"))
                .thenReturn(document);
        HTextFlow textFlow = new HTextFlow();
        when(textFlowDAO.getById(document, "resId")).thenReturn(textFlow);
        List<String> suggestion = Lists.newArrayList("mt suggestion");
        when(machineTranslationService.getSuggestion(textFlow, EN_US,
                new LocaleId("zh"))).thenReturn(suggestion);

        List<String> result = resource.getMachineTranslationSuggestion("proj",
                "ver", "docId", "resId", "zh");

        assertThat(result).isEqualTo(suggestion);

    }

}
