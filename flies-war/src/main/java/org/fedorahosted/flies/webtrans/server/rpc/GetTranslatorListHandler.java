package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.dao.AccountDAO;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.model.Person;
import org.fedorahosted.flies.webtrans.shared.model.PersonId;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorList;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorListResult;
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