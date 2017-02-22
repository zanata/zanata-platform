package org.zanata.rest.editor.service;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.SecurityService;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.TransMemoryMergeResource;
import org.zanata.webtrans.shared.rpc.TransMemoryMergeStarted;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
// we need to duplicate the Path annotation here because our classpath scanner only look for concrete classes
@Path(TransMemoryMergeResource.TM_PATH)
@RequestScoped
@Transactional
public class TransMemoryMergeResourceImpl implements TransMemoryMergeResource {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private TransMemoryMergeService transMemoryMergeServiceImpl;


    @Inject
    private SecurityService securityServiceImpl;

    @Override
    public TransMemoryMergeStarted merge(ProjectIterationId projectIterationId, String docId, String localeId) {
        identity.checkLoggedIn();

        try {
            securityServiceImpl.checkWorkspaceAction(new WorkspaceId(projectIterationId, new LocaleId(localeId)), SecurityService.TranslationAction.MODIFY);
        } catch (NoSuchWorkspaceException e) {
            throw new NotFoundException(e);
        }

        transMemoryMergeServiceImpl.executeMerge()

        TransMemoryMergeStarted response =
                new TransMemoryMergeStarted();
        response.numOfTransUnits = 100L;
        return response;
    }
}
