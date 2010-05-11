package org.fedorahosted.flies.webtrans.client.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.editor.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.client.ui.HighlightingLabel;

import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.user.client.ui.Label;

public class TableEditorTableDefinition extends DefaultTableDefinition<TransUnit> {
	
//	public static final int INDICATOR_COL = 0;
	public static final int SOURCE_COL = 0;
	public static final int TARGET_COL = 1;

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
			
			String state = "";
			switch(rowValue.getStatus()) {
			case Approved:
				state = " Approved";
				break;
			case NeedReview:
				state = " Fuzzy";
				break;
			case New:
				state = " New";
				break;
			}
			styles += state + "StateDecoration";
			
		    view.setStyleName( styles);
		}
	};
	
//	private final AbstractColumnDefinition<TransUnit, TransUnit> indicatorColumnDefinition = 
//		new AbstractColumnDefinition<TransUnit, TransUnit>() {
//		@Override
//		public TransUnit getCellValue(TransUnit rowValue) {
//			return rowValue;
//		}
//
//		@Override
//		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
//			cellValue.setSource(rowValue.getSource());
//		}
//	};
//
//	private final CellRenderer<TransUnit, TransUnit> indicatorCellRenderer = new CellRenderer<TransUnit, TransUnit>() {
//		@Override
//		public void renderRowValue(
//				TransUnit rowValue,
//				ColumnDefinition<TransUnit, TransUnit> columnDef,
//				com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView<TransUnit> view) {
//			view.setStyleName("TableEditorCell TableEditorCell-Source");
//			if(rowValue.getEditStatus().equals(EditState.Lock)) {
//				Image image = new Image("../img/silk/user.png");
//				view.setWidget(image);
//			}
//		}
//	};
	
	private final AbstractColumnDefinition<TransUnit, TransUnit> sourceColumnDefinition = 
			new AbstractColumnDefinition<TransUnit, TransUnit>() {
		@Override
		public TransUnit getCellValue(TransUnit rowValue) {
			return rowValue;
		}

		@Override
		public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
			cellValue.setSource(rowValue.getSource());
			cellValue.setSourceComment(rowValue.getSourceComment());
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
			final Label label = new HighlightingLabel(rowValue.getTarget());
			label.setStylePrimaryName("TableEditorContent");
			
			// TODO label.setTitle(rowValue.getTargetComment());
			
			view.setWidget( label );
		}
	};

	private InlineTargetCellEditor targetCellEditor;

	public TableEditorTableDefinition(final NavigationMessages messages, final RedirectingCachedTableModel<TransUnit> tableModel) {
		setRowRenderer(rowRenderer);
		//indicatorColumnDefinition.setMaximumColumnWidth(15);
		//indicatorColumnDefinition.setPreferredColumnWidth(15);
		//indicatorColumnDefinition.setMinimumColumnWidth(15);
		//indicatorColumnDefinition.setCellRenderer(indicatorCellRenderer);
		sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
		targetColumnDefinition.setCellRenderer(targetCellRenderer);
		CancelCallback<TransUnit> cancelCallBack = new CancelCallback<TransUnit>() {
			 @Override
			 public void onCancel(TransUnit cellValue) {
				 tableModel.onCancel(cellValue);
	         }
		};
		EditRowCallback transValueCallBack = new EditRowCallback() {
			 @Override
			 public void gotoNextRow(int row) {
				 tableModel.gotoNextRow(row);
	         }

			 @Override
			 public void gotoPrevRow(int row) {
				 tableModel.gotoPrevRow(row);
	         }
			 
			@Override
			public void gotoNextFuzzy(int row, ContentState state) {
				tableModel.gotoNextFuzzy(row, state);
			}

			@Override
			public void gotoPrevFuzzy(int row, ContentState state) {
				tableModel.gotoPrevFuzzy(row, state);
			}
		};
		this.targetCellEditor = new InlineTargetCellEditor(messages, cancelCallBack,transValueCallBack);
		targetColumnDefinition.setCellEditor(targetCellEditor);
		
		// See _INDEX consts above if modifying!
		//addColumnDefinition(indicatorColumnDefinition);
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

	public InlineTargetCellEditor getTargetCellEditor() {
		return targetCellEditor;
	}
	

}
