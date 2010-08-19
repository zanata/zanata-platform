package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.model.Person;
import net.openl10n.flies.webtrans.shared.model.PersonId;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorList;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorListResult;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.google.common.collect.ImmutableSet;

@Name("webtrans.gwt.GetTranslatorListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTranslatorList.class)
public class GetTranslatorListHandler extends AbstractActionHandler<GetTranslatorList, GetTranslatorListResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   AccountDAO accountDAO;

   @Override
   public GetTranslatorListResult execute(GetTranslatorList action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      TranslationWorkspace translationWorkspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      ImmutableSet<PersonId> personIdlist = translationWorkspace.getUsers();

      // Use AccountDAO to convert the PersonId to Person
      ArrayList<Person> translators = new ArrayList<Person>();
      for (PersonId personId : personIdlist)
      {
         Person translator = new Person(personId, accountDAO.getByUsername(personId.toString()).getPerson().getName());
         translators.add(translator);
      }

      return new GetTranslatorListResult(translators);
   }

   @Override
   public void rollback(GetTranslatorList action, GetTranslatorListResult result, ExecutionContext context) throws ActionException
   {
   }
}