package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.webtrans.client.ui.FilterTree;
import org.fedorahosted.flies.webtrans.client.ui.HasChildTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.HasFilter;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOutHandlers;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOverHandlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.Widget;

public class WorkspaceUsersView extends Composite implements
		WorkspaceUsersPresenter.Display {

	public interface Images extends ImageBundle, TreeImages {

		@Resource("org/fedorahosted/flies/webtrans/images/silk/world.png")
		AbstractImagePrototype treeOpen();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/world.png")
		AbstractImagePrototype treeClosed();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/user.png")
		AbstractImagePrototype treeLeaf();

	}

	private static Images images = (Images) GWT.create(Images.class);
	private FilterTree<PersonId, Person> tree;

	
	public WorkspaceUsersView() {
		tree = new FilterTree<PersonId, Person>(new PersonTreeNodeMapper(), images);	
		tree.setWidth("100%");
		
//		RoundedContainerWithHeader container = new RoundedContainerWithHeader(new Label("Translators in Workspace"), tree );
//		initWidget(container);
		
		DisclosurePanel container = new DisclosurePanel("Translators in Workspace", true);
		container.add(tree);
		initWidget(container);
		
		getElement().setId("WorkspaceUsersView");
		
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	public HasChildTreeNodes<Person> getTree() {
		return tree;
	}

	@Override
	public HasFilter<Person> getFilter() {
		return tree;
	}
	
	@Override
	public HasNodeMouseOverHandlers getNodeMouseOver() {
		return tree;
	}

	@Override
	public HasNodeMouseOutHandlers getNodeMouseOut() {
		return tree;
	}

}
