package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView.Images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ImageBundle.Resource;

public class DocumentListView extends Composite implements DocumentListPresenter.Display {

	public interface Images extends ImageBundle, TreeImages {

		@Resource("org/fedorahosted/flies/webtrans/images/silk/world.png")
		AbstractImagePrototype treeOpen();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/world.png")
		AbstractImagePrototype treeClosed();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/user.png")
		AbstractImagePrototype treeLeaf();

	}

	private static Images images = (Images) GWT.create(Images.class);
	
	public DocumentListView() {		

	    // Create a tree with a few items in it.
	    Tree root = new Tree(images);
	    
	    TreeItem item0 = new TreeItem("item0");
	    TreeItem item1 = new TreeItem("item1");
	    TreeItem item2 = new TreeItem("item2");
	    
	    root.addItem(item0);
	    root.addItem(item1);
	    root.addItem(item2);
	    
	    item0.addItem(new TreeItem("item0.1"));
	    item0.addItem(new TreeItem("item0.2"));
	    item0.addItem(new TreeItem("item0.3"));
	    
	    item1.addItem(new TreeItem("item1.1"));
	    item1.addItem(new TreeItem("item1.2"));
	    
	    item2.addItem(new TreeItem("item2.1"));
	    item2.addItem(new TreeItem("item2.2"));
	    item2.addItem(new TreeItem("item2.3"));
	    item2.addItem(new TreeItem("item2.4"));
	    
	    root.addItem(new TreeItem("item3"));

	    initWidget(root);

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
