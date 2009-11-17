package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.PersonDAO;
import org.fedorahosted.flies.core.model.HPerson;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.GetTranslatorList;
import org.fedorahosted.flies.gwt.rpc.GetTranslatorListResult;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
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
public class GetTranslatorListHandler implements ActionHandler<GetTranslatorList, GetTranslatorListResult> {

	@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;
	
	@In PersonDAO personDAO;

	@Override
	public GetTranslatorListResult execute(GetTranslatorList action, 
			ExecutionContext context) throws ActionException {

		FliesIdentity.instance().checkLoggedIn();
		
		TranslationWorkspace translationWorkspace = 
			translationWorkspaceManager.getOrRegisterWorkspace(action.getProjectContainerId().getId(), 
					new LocaleId(action.getLocaleId().getValue()));
		
		ImmutableSet<PersonId> personIdlist = translationWorkspace.getUsers();

		//Need to change the PersonIdlist to Person list
		
		Person[] translators = new Person[]{
				new Person( new PersonId("bob"), "Bob Smith"),
				new Person( new PersonId("jane"), "Jane English"),
				new Person( new PersonId("bill"), "Bill Martin")
		};	
		
		return new GetTranslatorListResult(translators);
	}

	@Override
	public Class<GetTranslatorList> getActionType() {
		return GetTranslatorList.class;
	}

	@Override
	public void rollback(GetTranslatorList action,
			GetTranslatorListResult result, ExecutionContext context)
			throws ActionException {
	}
}