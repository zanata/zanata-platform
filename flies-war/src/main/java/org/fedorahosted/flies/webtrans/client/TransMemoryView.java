package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransMemory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TransMemoryView extends FlowPanel implements TransMemoryPresenter.Display {

	private Button searchButton;
	private Button clearButton = new Button("Clear");
	private TextBox tmTextBox;
	
	private final FlexTable resultTable = new FlexTable();
	public TransMemoryView() {
		tmTextBox = new TextBox();
		searchButton = new Button("Search");
		
		tmTextBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					searchButton.click();
				}
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				tmTextBox.setText("");
				clearResults();
			}
		});
		
		add(tmTextBox);
		add(searchButton);
		add(clearButton);
		add(resultTable);
	}
	
	@Override
	public Button getSearchButton() {
		return searchButton;
	}
	
	public TextBox getTmTextBox() {
		return tmTextBox;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public void createTable(ArrayList<TransMemory> memories) {
		clearResults();
		Label source = new Label("Source");
		Label target = new Label("Target");
		source.setStyleName("TransMemoryTableColumnHeader");
		target.setStyleName("TransMemoryTableColumnHeader");
		resultTable.setWidget(0, 0, source);
		resultTable.setWidget(0, 1, target);
		int row = 1;
		for(TransMemory memory: memories) {
			resultTable.setWidget(row, 0, new Label(memory.getSource()));
			resultTable.setWidget(row, 1, new Label(memory.getMemory()));
			row++;
		}
		resultTable.setCellPadding(5);
	}
	
	@Override
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

