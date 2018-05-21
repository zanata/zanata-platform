package org.zanata.rest.service.raw;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.MinContentState;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.dto.resource.TranslationsResource;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MinContentStateRestITCase extends SourceAndTranslationResourceRestBase {
    private static final String MULTIPLE_TEXT_FLOW_DATA_DB_UNIT_XML =
            "org/zanata/test/model/MultipleTextFlowTestData.dbunit.xml";

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
                .getTranslationsWithDocId(LocaleId.DE, "my/path/document-5.txt", null, false, MinContentState.Translated, null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets()).hasSize(5);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Approved))).hasSize(2);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Translated))).hasSize(0);
    }

    @Test
    @RunAsClient
    public void testApproved() {
        Response getResponse = getTransResource()
                .getTranslationsWithDocId(LocaleId.DE, "my/path/document-5.txt", null, false, MinContentState.Approved, null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets()).hasSize(5);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Approved))).hasSize(1);
        assertThat(serverResource.getTextFlowTargets().stream().filter(textFlowTarget -> textFlowTarget.getState().equals(ContentState.Translated))).hasSize(1);
    }

}
