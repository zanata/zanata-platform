package org.zanata.webtrans.server.rpc;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

@Name("webtrans.gwt.GetTranslatorListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTranslatorList.class)
public class GetTranslatorListHandler extends AbstractActionHandler<GetTranslatorList, GetTranslatorListResult>
{

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   AccountDAO accountDAO;

   @In
   GravatarService gravatarServiceImpl;

   @Override
   public GetTranslatorListResult execute(GetTranslatorList action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      TranslationWorkspace translationWorkspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      Map<EditorClientId, PersonSessionDetails> result = translationWorkspace.getUsers();

      // Use AccountDAO to convert the PersonId to Person
      Map<EditorClientId, PersonSessionDetails> translators = new HashMap<EditorClientId, PersonSessionDetails>();
      for (Map.Entry<EditorClientId, PersonSessionDetails> entry : result.entrySet())
      {
         PersonId personId = entry.getValue().getPerson().getId();

         HPerson person = accountDAO.getByUsername(personId.toString()).getPerson();

         Person translator = new Person(personId, person.getName(), gravatarServiceImpl.getUserImageUrl(16, person.getEmail()));
         entry.getValue().setPerson(translator);
         translators.put(entry.getKey(), entry.getValue());
      }

      return new GetTranslatorListResult(translators, result.size());
   }

   @Override
   public void rollback(GetTranslatorList action, GetTranslatorListResult result, ExecutionContext context) throws ActionException
   {
   }
}