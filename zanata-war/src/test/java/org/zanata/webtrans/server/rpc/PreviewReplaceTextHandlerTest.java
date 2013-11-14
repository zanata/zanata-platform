package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;
import org.zanata.webtrans.shared.rpc.PreviewReplaceText;
import org.zanata.webtrans.shared.rpc.PreviewReplaceTextResult;
import org.zanata.webtrans.shared.rpc.ReplaceText;

import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class PreviewReplaceTextHandlerTest {
    private PreviewReplaceTextHandler handler;
    @Mock
    private ZanataIdentity identity;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
        handler = SeamAutowire.instance()
                .reset()
                .use("identity", identity)
                .autowire(PreviewReplaceTextHandler.class);
      // @formatter:on
    }

    @Test
    public void testExecute() throws Exception {
        TransUnit transUnit =
                TestFixture.makeTransUnit(1, ContentState.NeedReview, "target");
        PreviewReplaceText action =
                new PreviewReplaceText(new ReplaceText(transUnit, "target",
                        "replace", true));

        PreviewReplaceTextResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getPreviews(), Matchers.hasSize(1));
        TransUnitUpdatePreview preview = result.getPreviews().get(0);
        assertThat(preview.getId(), Matchers.equalTo(transUnit.getId()));
        assertThat(preview.getState(),
                Matchers.equalTo(ContentState.NeedReview));
        assertThat(preview.getContents(), Matchers.contains("replace"));
    }

    @Test(expectedExceptions = ActionException.class)
    public void cannotRollback() throws ActionException {
        handler.rollback(null, null, null);
    }
}
