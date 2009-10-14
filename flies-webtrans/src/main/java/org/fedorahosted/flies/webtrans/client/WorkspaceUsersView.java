package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;
import org.fedorahosted.flies.webtrans.client.ui.HeadingPanel;
import org.fedorahosted.flies.webtrans.client.ui.HeadingWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
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

	public WorkspaceUsersView() {
		super();
		addHead("Translators");
		addBody(getChatAllPanel());
		initPanel();
		/*
		HeadingWidget heading = new HeadingWidget("Translators");
		heading.setCollapsible(false);
		panel.setHeadingWidget(heading);
		*/
	}

	private static Tree createLocaleTranslatorsTree() {
		Tree tree = new Tree(images);
		tree.setWidth("100%");
		addLocaleData(tree, "German");
		return tree;
	}

	private static TreeItem createTranslator(String name) {
		TreeItem item = new TreeItem();
		item.setText(name);
		return item;
	}

	private static void addLocaleData(Tree tree, String locale) {
		TreeItem item = new TreeItem();
		item.setText(locale);
		tree.addItem(item);
		item.addItem(createTranslator("Bob"));
		item.addItem(createTranslator("Jane"));
	}

	public static Widget getChatAllPanel() {

		VerticalSplitPanel vSplit = new VerticalSplitPanel();
		vSplit.setBottomWidget(new Label("Chat"));
		vSplit.setWidth("200px");
		vSplit.setHeight("300px");
		vSplit.setTopWidget(createLocaleTranslatorsTree());
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

}
