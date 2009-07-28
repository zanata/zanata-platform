package org.fedorahosted.flies.webtrans.action;

import net.openl10n.packaging.jpa.document.HTextFlowTarget;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("flies.tftDataModel")
@AutoCreate
@Scope(ScopeType.CONVERSATION)
public class TextFlowTargetDataModel extends BaseModifiableHibernateDataModel<HTextFlowTarget> {

	@In(value="#{entityManager.delegate}")
	private Session hibernateSession;

	public TextFlowTargetDataModel() {
		super(HTextFlowTarget.class);
	}

	@Override
	protected Session getSession() {
		return hibernateSession;
	}

}
