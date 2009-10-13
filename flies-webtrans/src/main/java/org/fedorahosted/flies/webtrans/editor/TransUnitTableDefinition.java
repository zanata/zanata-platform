package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.webtrans.client.mvp.TextAreaCellEditor;
import org.fedorahosted.flies.webtrans.model.TransUnit;

import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultRowRenderer;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.gen2.table.client.TableDefinition.AbstractRowView;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class TransUnitTableDefinition extends DefaultTableDefinition<TransUnit> {
	
	//private final HasValue<ParserSyntax> parserSyntax;
	
	public TransUnitTableDefinition() {//(HasValue<ParserSyntax> parserSyntax) {
		// Set the row renderer
		setRowRenderer( new RowRenderer<TransUnit>() {
			@Override
			public void renderRowValue(TransUnit rowValue,
					AbstractRowView<TransUnit> view) {
			      view.setStyleName( view.getRowIndex() % 2 == 0 ? "odd-row" : "even-row");
			}
		});
		//this.parserSyntax = parserSyntax;

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
						Label label = new Label(rowValue.getSource());
						label.setStylePrimaryName("webtrans-editor-content");
						label.addStyleName("webtrans-editor-content-source");
						view.setWidget( label );
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

					Label label = new Label(rowValue.getTarget());
					label.setStylePrimaryName("webtrans-editor-content");
					label.addStyleName("webtrans-editor-content-target");
					view.setWidget( label );
				}
			});
			setCellEditor(new InlineTransUnitCellEditor());
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
