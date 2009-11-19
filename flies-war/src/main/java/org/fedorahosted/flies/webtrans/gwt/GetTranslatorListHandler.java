package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.core.dao.AccountDAO;
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
	
	@In AccountDAO accountDAO;

	@Override
	public GetTranslatorListResult execute(GetTranslatorList action, 
			ExecutionContext context) throws ActionException {

		FliesIdentity.instance().checkLoggedIn();
		
		TranslationWorkspace translationWorkspace = 
			translationWorkspaceManager.getOrRegisterWorkspace(action.getProjectContainerId().getId(), 
					action.getLocaleId() );
		
		ImmutableSet<PersonId> personIdlist = translationWorkspace.getUsers();

		//Use AccountDAO to convert the PersonId to Person
		ArrayList<Person> translators = new ArrayList<Person>();
		for(PersonId personId:personIdlist) {
			Person translator = new Person(personId, accountDAO.getByUsername(personId.toString()).getPerson().getName());
			translators.add(translator);
		}
		
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