package org.fedorahosted.flies.webtrans.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FilterTree<T> extends Composite implements HasTreeNodes<T>, HasFilter<T>, HasNodeMouseOverHandlers, HasNodeMouseOutHandlers {
	private final TreeNodeMapper<T> mapper;
	private final Panel panel = new VerticalPanel();
	private final FilterBox filterBox = new FilterBox();
	private final TreeImpl<T> tree;
	private final ArrayList<T> list = new ArrayList<T>();
	private final ArrayList<MouseOverHandler> mouseOverHandlers = new ArrayList<MouseOverHandler>();
	private final ArrayList<MouseOutHandler> mouseOutHandlers = new ArrayList<MouseOutHandler>();
	
	
	public FilterTree(TreeNodeMapper<T> mapper) {
		this.mapper = mapper;
		tree = new TreeImpl<T>();
		initWidgets();
	}

	public FilterTree(TreeNodeMapper<T> mapper, TreeImages images) {
		this.mapper = mapper;
		tree = new TreeImpl<T>(images);
		initWidgets();
	}
	
	private void initWidgets() {
		panel.add(filterBox);
	    Panel scrollPanel = new ScrollPanel();
	    scrollPanel.setWidth("100%");
	    scrollPanel.setHeight("150px");
	    scrollPanel.add(tree);
	    panel.add(scrollPanel);
		initWidget(panel);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		filterBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				filterBy(filterBox.getText());
			}
		});
	}
	
	
	@Override
	public void setList(List<T> list) {
		this.list.clear();
		this.list.addAll(list);
		filterBox.clearFilter();
		filterBy("");
	}

	public void clear() {
		tree.clear();
	}
	
	private void filterBy(String value) {
		tree.clear();

		if (value != null && value.length() != 0) {
			ArrayList<T> filteredNames = new ArrayList<T>();
			for (T docName : list) {
				if (mapper.passFilter(docName, value)) {
					filteredNames.add(docName);
				}
			}
			mapper.addToTree(tree, filteredNames, true);
		} else {
			mapper.addToTree(tree, list, false);
		}
		for (MouseOverHandler handler : mouseOverHandlers)
			addMouseOverHandlerToTreeNodes(handler);
	}

	private void addMouseOverHandlerToTreeNodes(MouseOverHandler handler) {
		addMouseOverHandlerToChildNodes(tree, handler);
	}

	private void addMouseOverHandlerToChildNodes(HasChildTreeNodes<T> subtree, MouseOverHandler handler) {
		// TODO recursive
		for (int i = 0; i < subtree.getNodeCount(); i++) {
			final TreeNode<T> node = subtree.getNode(i);
			if (node.getObject() == null) {
				addMouseOverHandlerToChildNodes(node, handler);
			} else {
				node.addMouseOverHandler(handler);
			}
		}
	}
	
	private void addMouseOutHandlerToTreeNodes(MouseOutHandler handler) {
		addMouseOutHandlerToChildNodes(tree, handler);
	}

	private void addMouseOutHandlerToChildNodes(HasChildTreeNodes<T> subtree, MouseOutHandler handler) {
		// TODO recursive
		for (int i = 0; i < subtree.getNodeCount(); i++) {
			final TreeNode<T> node = subtree.getNode(i);
			if (node.getObject() == null) {
				addMouseOutHandlerToChildNodes(node, handler);
			} else {
				node.addMouseOutHandler(handler);
			}
		}
	}
	
	public TreeNode<T> getNode(int index) {
		return tree.getNode(index);
	}

	public int getNodeCount() {
		return tree.getNodeCount();
	}

	@Override
	public TreeNode<T> addItem(String name) {
		return tree.addItem(name);
	}

	@Override
	public TreeNodeImpl<T> getSelectedNode() {
		return tree.getSelectedNode();
	}

	@Override
	public void removeItems() {
		tree.removeItems();
	}

	@Override
	public void setSelectedNode(TreeNode<T> node) {
		tree.setSelectedNode(node);
	}

	@Override
	public HandlerRegistration addSelectionHandler(
			SelectionHandler<TreeItem> handler) {
		return tree.addSelectionHandler(handler);
	}

	@Override
	public HandlerRegistration addNodeMouseOverHandler(final MouseOverHandler handler) {
		addMouseOverHandlerToTreeNodes(handler);
		mouseOverHandlers.add(handler);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				mouseOverHandlers.remove(handler);
			}
		};
	}

	@Override
	public HandlerRegistration addNodeMouseOutHandler(final MouseOutHandler handler) {
		addMouseOutHandlerToTreeNodes(handler);
		mouseOutHandlers.add(handler);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				mouseOutHandlers.remove(handler);
			}
		};
	}
	
}
