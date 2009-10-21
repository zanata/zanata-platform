package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;
import org.fedorahosted.flies.webtrans.client.ui.FilterBox;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeImpl;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeImpl;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
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
	private TreeImpl<Object> tree;

	
	public WorkspaceUsersView() {
		super();
		tree = new TreeImpl<Object>(images);	
		setTitle("Translators");
		setBody(getChatAllPanel());
	}

	private Panel createLocaleTranslatorsTree() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
//		tree.setWidth("100%");
		addLocaleData(tree, "German", "");
		
		final FilterBox tb = new FilterBox();
		tb.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					tree.clear();
					addLocaleData(tree, "German", tb.getText());
				}
			}
		});
		panel.add(tb);
		panel.add(tree);
		
		return panel;
	}


	private static void addLocaleData(TreeImpl<Object> tree, String locale, String filter) {
		TreeNode localeNode = tree.addItem(locale);
		
		for(Person translator : translators){
			if(filter.isEmpty() || translator.getName().contains(filter)){
				TreeNode node = localeNode.addItem(translator.getName());
				node.setObject(translator);
				node.addMouseOverHandler(new MouseOverHandler() {
					
					@Override
					public void onMouseOver(MouseOverEvent event) {
//						label.setText("selected");
						Log.info("");
					}
					
				});
			}
		}
	}
	
	private static final Person [] translators = new Person[]{
		new Person( new PersonId("bob"), "Bob"),
		new Person( new PersonId("jane"), "Jane"),
		new Person( new PersonId("Bill"), "Bill")
		};

	public Widget getChatAllPanel() {

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

	@Override
	public HasTreeNodes<Object> getTree() {
		return tree;
	}

}
