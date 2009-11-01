package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.mvp.TextAreaCellEditor;
import org.fedorahosted.flies.webtrans.editor.filter.ContentFilter;

import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultRowRenderer;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.gen2.table.client.TableDefinition.AbstractRowView;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class TableEditorTableDefinition extends DefaultTableDefinition<TransUnit> {

	private ContentFilter<TransUnit> contentFilter = null;
	
	private final RowRenderer<TransUnit> rowRenderer = new RowRenderer<TransUnit>() {
		@Override
		public void renderRowValue(TransUnit rowValue,
				AbstractRowView<TransUnit> view) {
			String styles = "TableEditorRow ";
			styles += view.getRowIndex() % 2 == 0 ? "odd-row" : "even-row";
			
			if(contentFilter != null) {
				styles += " content-filter";
				styles += contentFilter.accept(rowValue) ? " content-filter-match" : " content-filter-nomatch";
			}
			
			String state = rowValue.isFuzzy() ? " Fuzzy" : rowValue.getTarget().isEmpty() ? " New" : " Approved";
			styles += state + "StateDecoration";
			
		    view.setStyleName( styles);
		}
	};
	
	private final AbstractColumnDefinition<TransUnit, TransUnit> sourceColumnDefinition = 
			new AbstractColumnDefinition<TransUnit, TransUnit>() {
		@Override
		public TransUnit getCellValue(TransUnit rowValue) {
			return rowValue;
		}

		@Override
		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
			cellValue.setSource(rowValue.getSource());
		}
	};
	
	private final CellRenderer<TransUnit, TransUnit> sourceCellRenderer = new CellRenderer<TransUnit, TransUnit>() {
		@Override
		public void renderRowValue(
				TransUnit rowValue,
				ColumnDefinition<TransUnit, TransUnit> columnDef,
				com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView<TransUnit> view) {
			view.setStyleName("TableEditorCell TableEditorCell-Source");
			SourcePanel sourcePanel = new SourcePanel(rowValue);
			view.setWidget( sourcePanel );
		}
	};
	
	private final AbstractColumnDefinition<TransUnit, TransUnit> targetColumnDefinition = 
		new AbstractColumnDefinition<TransUnit, TransUnit>() {
		
		@Override
		public TransUnit getCellValue(TransUnit rowValue) {
			return rowValue;
		}
		
		@Override
		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
			cellValue.setTarget(rowValue.getTarget());
		}
	
	};
	
	private final CellRenderer<TransUnit, TransUnit> targetCellRenderer = new CellRenderer<TransUnit, TransUnit>() {
		@Override
		public void renderRowValue(
				TransUnit rowValue,
				ColumnDefinition<TransUnit, TransUnit> columnDef,
				AbstractCellView<TransUnit> view) {
			view.setStyleName("TableEditorCell TableEditorCell-Target");
			Label label = new HighlightingLabel(rowValue.getTarget(), ParserSyntax.MIXED);
			label.setStylePrimaryName("TableEditorContent");
			view.setWidget( label );
		}
	};

	public TableEditorTableDefinition() {
		setRowRenderer(rowRenderer);

		sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
		targetColumnDefinition.setCellRenderer(targetCellRenderer);
		targetColumnDefinition.setCellEditor(new InlineTargetCellEditor());
		
		addColumnDefinition(sourceColumnDefinition);
		addColumnDefinition(targetColumnDefinition);
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
