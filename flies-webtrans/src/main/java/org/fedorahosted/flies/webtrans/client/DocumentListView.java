package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;
import org.fedorahosted.flies.webtrans.client.ui.HasTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.TreeImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DocumentListView extends CaptionPanel 
	implements DocumentListPresenter.Display {

	public interface Images extends ImageBundle, TreeImages {

		@Resource("org/fedorahosted/flies/webtrans/images/silk/folder.png")
		AbstractImagePrototype treeOpen();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/folder_page_white.png")
		AbstractImagePrototype treeClosed();

		@Resource("org/fedorahosted/flies/webtrans/images/silk/page_white_text.png")
		AbstractImagePrototype treeLeaf();

	}

	private static Images images = (Images) GWT.create(Images.class);
	private TreeImpl<DocName> tree;
	private TextBox filterTextBox = new TextBox();
	
	public DocumentListView() {		

		super();
		GWT.log("DocumentListView()", null);
	    tree = new TreeImpl<DocName>(images);
	    VerticalPanel treePanel = new VerticalPanel();
	    treePanel.setWidth("100%");
	    treePanel.setHeight("150px");
	    treePanel.add(new ScrollPanel(tree));
	    
	    setTitle("Documents");
	    filterTextBox.setText("search");
	    filterTextBox.setWidth("100%");
	    addBody(filterTextBox);
	    addBody(treePanel);
	    
	    filterTextBox.addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				filterTextBox.setText("");
			}
	    	
	    });
	    
	    filterTextBox.addBlurHandler(new BlurHandler() {

			@Override
			public void onBlur(BlurEvent event) {
				filterTextBox.setText("search");
			}
	    	
	    });
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
	public HasTreeNodes<DocName> getTree() {
		return tree;
	}

	@Override
	public HasValueChangeHandlers<String> getFilterChangeSource() {
		return filterTextBox;
	}

	@Override
	public HasKeyUpHandlers getFilterKeyUpSource() {
		return filterTextBox;
	}

	@Override
	public HasText getFilterText() {
		return filterTextBox;
	}

}
