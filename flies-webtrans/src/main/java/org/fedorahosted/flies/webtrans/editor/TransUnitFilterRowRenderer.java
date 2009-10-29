package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.editor.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.editor.filter.PhraseFilter;

import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.gen2.table.client.TableDefinition.AbstractRowView;
import com.google.inject.Inject;

public class TransUnitFilterRowRenderer implements RowRenderer<TransUnit>{
	
	private ContentFilter<TransUnit> contentFilter = null;
	
	@Override
	public void renderRowValue(TransUnit rowValue,
			AbstractRowView<TransUnit> view) {
		String styles = "";
		styles += view.getRowIndex() % 2 == 0 ? "odd-row" : "even-row";
		
		if(contentFilter != null) {
			styles += " content-filter";
			styles += contentFilter.accept(rowValue) ? " content-filter-match" : " content-filter-nomatch";
		}
		
	    view.setStyleName( styles);
	}

	public void clearContentFilter() {
		this.contentFilter = null;
	}
	
	public void setContentFilter(ContentFilter<TransUnit> contentFilter) {
		this.contentFilter = contentFilter;
	}
	
	public ContentFilter<TransUnit> getContentFilter() {
		return contentFilter;
	}
}
