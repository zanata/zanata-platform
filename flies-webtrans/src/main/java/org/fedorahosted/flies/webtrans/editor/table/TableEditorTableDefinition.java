package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.mvp.TextAreaCellEditor;

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
	
	public TableEditorTableDefinition() {
		addColumnDefinition(new SourceColumnDefinition());
		addColumnDefinition(new TargetColumnDefinition());
	}

	
	private static class SourceColumnDefinition extends AbstractColumnDefinition<TransUnit, TransUnit> {
		
		public SourceColumnDefinition() {
			setCellRenderer(new CellRenderer<TransUnit, TransUnit>() {
				@Override
				public void renderRowValue(
						TransUnit rowValue,
						ColumnDefinition<TransUnit, TransUnit> columnDef,
						AbstractCellView<TransUnit> view) {
					SourcePanel sourcePanel = new SourcePanel(rowValue);
					view.setWidget( sourcePanel );
				}
			});
		}
		
		@Override
		public TransUnit getCellValue(TransUnit rowValue) {
			return rowValue;
		}
		
		@Override
		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
			cellValue.setSource(rowValue.getSource());
		}
	
	}

	
	private static class TargetColumnDefinition extends AbstractColumnDefinition<TransUnit, TransUnit> {
		
		public TargetColumnDefinition() {
			setCellRenderer(new CellRenderer<TransUnit, TransUnit>() {
				@Override
				public void renderRowValue(
						TransUnit rowValue,
						ColumnDefinition<TransUnit, TransUnit> columnDef,
						AbstractCellView<TransUnit> view) {

					Label label = new HighlightingLabel(rowValue.getTarget(), ParserSyntax.MIXED);
					label.setStylePrimaryName("webtrans-editor-content");
					label.addStyleName("webtrans-editor-content-target");
					view.setWidget( label );
				}
			});
			setCellEditor(new InlineTargetCellEditor());
		}
		
		@Override
		public TransUnit getCellValue(TransUnit rowValue) {
			return rowValue;
		}
		
		@Override
		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
			cellValue.setTarget(rowValue.getTarget());
		}
	
	}
}
