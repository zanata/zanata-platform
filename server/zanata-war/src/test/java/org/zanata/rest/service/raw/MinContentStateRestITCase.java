package org.zanata.rest.service.raw;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.dto.resource.TranslationsResource;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MinContentStateRestITCase extends SourceAndTranslationResourceRestBase {

    // MinContentStateRest data
    private static final int APPROVED_TFTS = 1;
    private static final int TRANSLATED_TFTS = 1;
    private static final int ALL_TFTS =  5;

    private static final String MULTIPLE_TEXT_FLOW_DATA_DB_UNIT_XML =
            "org/zanata/test/model/MinContentStateRestITCase.dbunit.xml";

    private static final String DOCUMENTS_DATA_DBUNIT_XML =
            "org/zanata/test/model/DocumentsData.dbunit.xml";

    @Override
    protected void prepareDBUnitOperations() {
        super.prepareDBUnitOperations();

        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));

        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(MULTIPLE_TEXT_FLOW_DATA_DB_UNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void testTranslated() {
        Response getResponse = getTransResource()
                .getTranslationsWithDocId(LocaleId.DE, "my/path/document-5.txt", null, false, ContentState.Translated.toString(), null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets()).hasSize(ALL_TFTS);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Approved))).hasSize(APPROVED_TFTS + TRANSLATED_TFTS);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Translated))).hasSize(0);
    }

    @Test
    @RunAsClient
    public void testApproved() {
        Response getResponse = getTransResource()
                .getTranslationsWithDocId(LocaleId.DE, "my/path/document-5.txt", null, false, ContentState.Approved.toString(), null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets()).hasSize(ALL_TFTS);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Approved))).hasSize(APPROVED_TFTS);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Translated))).hasSize(TRANSLATED_TFTS);
    }

}
