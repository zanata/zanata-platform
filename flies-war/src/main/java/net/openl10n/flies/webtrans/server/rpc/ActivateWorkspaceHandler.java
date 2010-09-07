package net.openl10n.flies.webtrans.server.rpc;

import java.util.HashSet;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.auth.Permission;
import net.openl10n.flies.webtrans.shared.auth.Role;
import net.openl10n.flies.webtrans.shared.auth.SessionId;
import net.openl10n.flies.webtrans.shared.model.Person;
import net.openl10n.flies.webtrans.shared.model.PersonId;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import net.openl10n.flies.webtrans.shared.rpc.EnterWorkspace;

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