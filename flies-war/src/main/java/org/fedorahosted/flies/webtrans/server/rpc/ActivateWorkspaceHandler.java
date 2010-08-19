package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.HashSet;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.model.HPerson;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.auth.Permission;
import org.fedorahosted.flies.webtrans.shared.auth.Role;
import org.fedorahosted.flies.webtrans.shared.auth.SessionId;
import org.fedorahosted.flies.webtrans.shared.model.Person;
import org.fedorahosted.flies.webtrans.shared.model.PersonId;
import org.fedorahosted.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.webtrans.shared.rpc.EnterWorkspace;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.web.ServletContexts;

@Name("webtrans.gwt.ActivateWorkspaceHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ActivateWorkspaceAction.class)
public class ActivateWorkspaceHandler extends AbstractActionHandler<ActivateWorkspaceAction, ActivateWorkspaceResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public ActivateWorkspaceResult execute(ActivateWorkspaceAction action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      workspace.registerTranslator(ActivateWorkspaceHandler.retrieveSessionId(), ActivateWorkspaceHandler.retrievePersonId());

      // Send EnterWorkspace event to clients
      EnterWorkspace event = new EnterWorkspace(new PersonId(FliesIdentity.instance().getPrincipal().getName()));
      workspace.publish(event);

      Identity identity = new Identity(retrieveSessionId(), retrievePerson(), new HashSet<Permission>(), new HashSet<Role>());

      return new ActivateWorkspaceResult(workspace.getWorkspaceContext(), identity);
   }

   @Override
   public void rollback(ActivateWorkspaceAction action, ActivateWorkspaceResult result, ExecutionContext context) throws ActionException
   {
   }

   public static SessionId retrieveSessionId()
   {
      return new SessionId(ServletContexts.instance().getRequest().getSession().getId());
   }

   public static PersonId retrievePersonId()
   {
      HPerson authenticatedPerson = (HPerson) Contexts.getSessionContext().get("authenticatedPerson");
      return new PersonId(authenticatedPerson.getAccount().getUsername());
   }

   public static Person retrievePerson()
   {
      HPerson authenticatedPerson = (HPerson) Contexts.getSessionContext().get("authenticatedPerson");
      return new Person(new PersonId(authenticatedPerson.getAccount().getUsername()), authenticatedPerson.getName());
   }

}