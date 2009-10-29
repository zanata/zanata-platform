package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class FilterPack extends HorizontalPanel {

	private Label titleLabel;
	private TextBox inputBox;
	private Button deleteButton;
	
	public FilterPack() {
		this.titleLabel = new Label();
		this.inputBox = new TextBox();
		this.deleteButton = new Button("X");
	}
	
	public FilterPack(String title, TextBox inputBox) {
		this.titleLabel = new Label(title);
		this.inputBox = inputBox;
		this.deleteButton = new Button("X");
	}
	
	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}
	
	public String getTitle() {
		return this.titleLabel.getText();
	}

	public void setInputBox(TextBox inputBox) {
		this.inputBox = inputBox;
	}
	
	public TextBox getInputBox() {
		return this.inputBox;
	}
	
	public HorizontalPanel getPack() {
		return this;
	}
	
}
