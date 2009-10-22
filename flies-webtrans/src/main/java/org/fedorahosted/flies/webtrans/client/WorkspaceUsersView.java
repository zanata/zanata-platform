package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;
import org.fedorahosted.flies.webtrans.client.ui.FilterTree;
import org.fedorahosted.flies.webtrans.client.ui.HasChildTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.HasFilter;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOverHandlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkspaceUsersView extends CaptionPanel implements
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
	private FilterTree<Person> tree;

	
	public WorkspaceUsersView() {
		super();
//		tree = new FilterTree<Person>(new PersonLocaleTreeNodeMapper(), images);	
		tree = new FilterTree<Person>(new PersonTreeNodeMapper(), images);	
//		tree.setWidth("100%");
		setTitle("Translators");
		setBody(getChatAllPanel());
	}

	public Widget getChatAllPanel() {

		VerticalSplitPanel vSplit = new VerticalSplitPanel();
		vSplit.setBottomWidget(new Label("Chat"));
		vSplit.setWidth("200px");
		vSplit.setHeight("300px");
		vSplit.setTopWidget(tree);
		return vSplit;
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

}
