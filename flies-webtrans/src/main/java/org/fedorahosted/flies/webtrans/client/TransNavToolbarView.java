package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class TransNavToolbarView extends HorizontalPanel {
	
	private Button
						nextEntryButton, prevEntryButton, 
						nextFuzzyButton, prevFuzzyButton,
						nextUntranslatedButton, prevUntranslatedButton;
	
	public TransNavToolbarView() {
		super();
		
		prevEntryButton = new Button("Prev Entry"); 
		nextEntryButton = new Button("Next Entry");
		prevFuzzyButton = new Button("Prev Fuzzy");
		nextFuzzyButton = new Button("next Fuzzy");
		prevUntranslatedButton = new Button("Prev Untranslated");
		nextUntranslatedButton = new Button("Next Untranslated");
		
		add(prevEntryButton);
		add(nextEntryButton);
		add(prevFuzzyButton);
		add(nextFuzzyButton);
		add(prevUntranslatedButton);
		add(nextUntranslatedButton);
		
	}

	public Button getPrevEntryButton() {
		return prevEntryButton;
	}
	
	public Button getNextEntryButton() {
		return nextEntryButton;
	}
	
	public Button getPrevFuzzyButton() {
		return prevFuzzyButton;
	}
	
	public Button getNextFuzzyButton() {
		return nextFuzzyButton;
	}
	
	public Button getPrevUntranslatedButton() {
		return prevUntranslatedButton;
	}
	
	public Button getNextUntranslatedButton() {
		return nextUntranslatedButton;
	}
}
