package org.fedorahosted.flies.webtrans.client.ui;

import org.fedorahosted.flies.webtrans.client.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ClearableTextBox extends Composite {

	private static ClearableTextBoxUiBinder uiBinder = GWT
			.create(ClearableTextBoxUiBinder.class);

	interface ClearableTextBoxUiBinder extends
			UiBinder<Widget, ClearableTextBox> {
	}
	
	interface Styles extends CssResource {
		String emptyBox();
	}	
	
	String emptyText = "Type to filter";

	@UiField
	TextBox textBox;
	
	@UiField
	Image xButton;

	@UiField(provided=true)
	final Resources resources;
	
	@UiField
	Styles style;	
	
	public ClearableTextBox() {
		this((Resources) GWT.create(Resources.class));
	}
	
	@Inject
	public ClearableTextBox(final Resources resources) {
		this.resources = resources;
		initWidget(uiBinder.createAndBindUi(this));
		xButton.setVisible( !textBox.getValue().isEmpty() );
		textBox.setText(emptyText);
		textBox.addStyleName(style.emptyBox());
	}
	
	@UiHandler("xButton")
	public void onXButtonClick(ClickEvent event) {
		textBox.setValue("", true);
		textBox.setValue(emptyText);
		textBox.addStyleName(style.emptyBox());
	}
	
	@UiHandler("textBox")
	public void onTextBoxValueChange(ValueChangeEvent<String> event) {
		xButton.setVisible( !event.getValue().isEmpty() );
	}

	@UiHandler("textBox")
	public void onTextBoxFocus(FocusEvent event) {
		if(textBox.getStyleName().contains(style.emptyBox())) {
			textBox.setValue("");
			textBox.removeStyleName(style.emptyBox());
		}
	}
	
	@UiHandler("textBox")
	public void onTextBoxBlur(BlurEvent event) {
		refresh();
	}
	
	private void refresh() {
		if(textBox.getText().isEmpty() || textBox.getStyleName().contains(style.emptyBox())) {
			textBox.setValue(emptyText);
			textBox.addStyleName(style.emptyBox());
		}
	}
	
	public void setEmptyText(String emptyText) {
		this.emptyText = emptyText;
		refresh();
	}
	
	
	public TextBox getTextBox() {
		return textBox;
	}
	
}
