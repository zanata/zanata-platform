import React, {PureRenderMixin} from 'react/addons';
import Actions from '../../actions/GlossaryActions';
import {Table, Column} from 'fixed-data-table';
import { Button, Icon, Tooltip, OverlayTrigger } from 'zanata-ui';
import StringUtils from '../../utils/StringUtils'
import InputCell from './InputCell';
import LoadingCell from './LoadingCell'
import ActionCell from './ActionCell'
import SourceActionCell from './SourceActionCell'
import ColumnHeader from './ColumnHeader'
import NewEntryModal from './NewEntryModal'
import ImportModal from './ImportModal'
import _ from 'lodash';

var DataTable = React.createClass({
  TIMEOUT: 400,

  NO_ROW: -1,

  CONTENT_HASH_INDEX: 0,

  loadTimeout: null,

  ENTRY: {
    SRC: {
      col: 1,
      field: 'srcTerm.content',
      sort_field: 'src_content'
    },
    TRANS: {
      col: 2,
      field: 'transTerm.content',
      sort_field: 'trans_content'
    },
    POS: {
      col: 3,
      field: 'pos',
      sort_field: 'part_of_speech'
    },
    DESC: {
      col: 4,
      field: 'description',
      sort_field: 'desc'
    },
    TRANS_COUNT: {
      col: 5,
      field: 'termsCount',
      sort_field: 'trans_count'
    }
  },
  CELL_HEIGHT: 48,

  propTypes: {
    glossaryData: React.PropTypes.object.isRequired,
    glossaryIds: React.PropTypes.arrayOf(
      React.PropTypes.arrayOf(React.PropTypes.number)
    ),
    canAddNewEntry: React.PropTypes.bool.isRequired,
    canUpdateEntry: React.PropTypes.bool.isRequired,
    user: React.PropTypes.shape({
      username: React.PropTypes.string,
      email: React.PropTypes.string,
      name: React.PropTypes.string,
      imageUrl: React.PropTypes.string,
      languageTeams: React.PropTypes.string
    }),
    srcLocale: React.PropTypes.shape({
      locale: React.PropTypes.shape({
        localeId: React.PropTypes.string.isRequired,
        displayName: React.PropTypes.string.isRequired,
        alias: React.PropTypes.string.isRequired
      }).isRequired,
      numberOfTerms: React.PropTypes.number.isRequired
    }),
    selectedTransLocale: React.PropTypes.string,
    totalCount: React.PropTypes.number.isRequired,
    focusedRow: React.PropTypes.shape({
      id: React.PropTypes.number,
      rowIndex: React.PropTypes.number
    }),
    locales: React.PropTypes.object,
    allowNewEntry: React.PropTypes.bool,
    loading: React.PropTypes.bool,
    filter: React.PropTypes.string
  },

  getInitialState: function () {
    var top = 246; //top height for banner if can't get height from dom
    return {
      tbl_width: this._getWidth(),
      tbl_height: this._getHeight(top),
      row_height: this.CELL_HEIGHT,
      header_height: this.CELL_HEIGHT,
      hoveredRow: -1
    }
  },

  /**
   * @param  top : number - the position of the top of the DataTable. If not supplied, the top position will be calculated based on DOM height.
   */
  _getHeight: function(top) {
    var footer = window.document.getElementById("footer");
    var footerHeight = footer ? footer.clientHeight : 91;

    top = _.isUndefined(top) ? React.findDOMNode(this).offsetTop: top;
    var newHeight = window.innerHeight - footerHeight - top;

    //minimum height 250px
    return Math.max(newHeight, 250);
  },

  _getWidth: function () {
    return window.innerWidth - 48;
  },

  _handleResize: function(e) {
    this.setState({tbl_height: this._getHeight(), tbl_width: this._getWidth()});
  },

  componentDidMount: function() {
    window.addEventListener('resize', this._handleResize);
  },

  componentWillUnmount: function() {
    if(this.loadTimeout !== null) {
      clearTimeout(this.loadTimeout);
    }

    window.removeEventListener('resize', this._handleResize);
  },

  _generateTermInfo: function(term) {
    var title = "";
    if(!_.isUndefined(term) && !_.isNull(term)) {
      if (!StringUtils.isEmptyOrNull(term.lastModifiedBy)
        || !StringUtils.isEmptyOrNull(term.lastModifiedDate)) {
        const parts = ['Last updated'];
        if (!StringUtils.isEmptyOrNull(term.lastModifiedBy)) {
          parts.push('by: ');
          parts.push(term.lastModifiedBy);
        }
        if (!StringUtils.isEmptyOrNull(term.lastModifiedDate)) {
          parts.push(term.lastModifiedDate);
        }
        title = parts.join(' ');
      }
    }
    if(StringUtils.isEmptyOrNull(title)) {
      title = "No information available";
    }
    return title;
  },

  _generateKey: function (colIndex, rowIndex, id) {
    var key = colIndex + ":" + rowIndex + ":" + id;
    if(this.props.selectedTransLocale) {
      key += ":" + this.props.selectedTransLocale;
    }
    return key;
  },

  _renderSourceHeader: function (label) {
    var key = this.ENTRY.SRC.sort_field;
    return this._renderHeader(label, key, true);
  },

  _renderTransHeader: function (label) {
    var key = this.ENTRY.TRANS.sort_field;
    return this._renderHeader(label, key, false);
  },

  _renderPosHeader: function (label) {
    var key = this.ENTRY.POS.sort_field;
    return this._renderHeader(label, key, true);
  },

  _renderDescHeader: function (label) {
    var key = this.ENTRY.DESC.sort_field;
    return this._renderHeader(label, key, true);
  },

  _renderTransCountHeader: function (label) {
    var key = this.ENTRY.TRANS_COUNT.sort_field;
    return this._renderHeader(label, key, true);
  },

  _renderHeader: function (label, key, allowSort) {
    return (
      <ColumnHeader
        value={label}
        field={key}
        key={key}
        allowSort={allowSort}
        onClickCallback={this._onHeaderClick}/>
    );
  },

  _onHeaderClick: function (field, ascending) {
    Actions.updateSortOrder(field, ascending);
  },

  _renderCell: function ({ id, rowIndex, field, readOnly, placeholder, maxLength, tooltip }) {
    var key = this._generateKey(field.col, rowIndex, id)
    if (id === null) {
      return <LoadingCell key={key}/>;
    } else {
      const entry = this._getGlossaryEntry(id)
      const value = _.get(entry, field.field)
      const tooltipContent = tooltip ? (<Tooltip id={key}>{value}</Tooltip>) : null
      const readonlyValue = <span className="mh1/2 db ovh tove" key={key}>{value}</span>
      if (readOnly && tooltip) {
        return (
          <OverlayTrigger placement='top' rootClose overlay={tooltipContent}>
            {readonlyValue}
          </OverlayTrigger>
        )
      } else if (readOnly) {
        return readonlyValue
      } else {
        return (
          <InputCell
            value={value}
            id={id}
            key={key}
            maxLength={maxLength}
            placeholder={placeholder}
            rowIndex={rowIndex}
            field={field.field}
            onFocusCallback={this._onRowClick}/>
        );
      }
    }
  },

  _renderSourceCell: function (id, cellDataKey, rowData, rowIndex,
                               columnData, width) {
    return this._renderCell({
      id: id,
      rowIndex: rowIndex,
      field: this.ENTRY.SRC,
      readOnly: true,
      placeholder: ''
    });
  },

  _renderTransCell: function(id, cellDataKey, rowData, rowIndex,
                             columnData, width) {
    var readOnly = !this.props.canUpdateEntry,
      placeholder = 'enter a translation';
    return this._renderCell({
      id: id,
      rowIndex: rowIndex,
      field: this.ENTRY.TRANS,
      readOnly: readOnly,
      placeholder: placeholder
    });
  },

  _renderPosCell: function (id, cellDataKey, rowData, rowIndex,
                            columnData, width) {
    var readOnly = !this.props.canUpdateEntry || this._isTranslationSelected(),
      placeholder = 'enter part of speech';
    return this._renderCell({
      id: id,
      rowIndex: rowIndex,
      field: this.ENTRY.POS,
      readOnly: readOnly,
      placeholder: placeholder,
      maxLength: 255
    });
  },

  _renderDescCell: function (id, cellDataKey, rowData, rowIndex,
                             columnData, width) {
    var readOnly = !this.props.canUpdateEntry || this._isTranslationSelected(),
      placeholder = 'enter description';
    return this._renderCell({
      id: id,
      rowIndex: rowIndex,
      field: this.ENTRY.DESC,
      readOnly: readOnly,
      placeholder: placeholder,
      maxLength: 255,
      tooltip: true
    });
  },

  _renderTransCountCell: function (id, cellDataKey, rowData, rowIndex,
                              columnData, width) {
    return this._renderCell({
      id: id,
      rowIndex: rowIndex,
      field: this.ENTRY.TRANS_COUNT,
      readOnly: true,
      placeholder: ''
    });
  },

  _renderActionCell: function (id, cellDataKey, rowData, rowIndex,
                            columnData, width) {
    if(id === null) {
      return <LoadingCell/>;
    } else if(!this.props.canUpdateEntry && !this.props.canAddNewEntry) {
      return;
    }
    var entry = this._getGlossaryEntry(id);
    if(this._isTranslationSelected()) {
      var info = this._generateTermInfo(entry.transTerm);
      return (
        <ActionCell info={info}
          canUpdateEntry={this.props.canUpdateEntry}
          id={id}
          rowIndex={rowIndex}/>
      );
    } else {
      var info = this._generateTermInfo(entry.srcTerm);
      return (
        <SourceActionCell id={id} rowIndex={rowIndex}
          srcLocaleId={this.props.srcLocale.locale.localeId}
          info={info}
          canUpdateEntry={this.props.canUpdateEntry}
          canDeleteEntry={this.props.canAddNewEntry}/>
      );
    }
  },

  _isTranslationSelected: function () {
    return !StringUtils.isEmptyOrNull(this.props.selectedTransLocale);
  },

  _getSourceColumn: function() {
    var srcLocaleName = "";
    if(!_.isUndefined(this.props.srcLocale) && !_.isNull(this.props.srcLocale)) {
      srcLocaleName = this.props.srcLocale.locale.displayName;
    }
    return (
      <Column
        label={srcLocaleName}
        key={this.ENTRY.SRC.field}
        width={150}
        dataKey={0}
        flexGrow={1}
        cellRenderer={this._renderSourceCell}
        headerRenderer={this._renderSourceHeader}/>
    );
  },

  _getTransColumn: function() {
    return (
      <Column
        label="Translations"
        key={this.ENTRY.TRANS.field}
        width={150}
        dataKey={0}
        flexGrow={1}
        cellRenderer={this._renderTransCell}
        headerRenderer={this._renderTransHeader}/>
    );
  },

  _getPosColumn: function() {
    return (
      <Column
        label="Part of Speech"
        key={this.ENTRY.POS.field}
        width={150}
        dataKey={0}
        cellRenderer={this._renderPosCell}
        headerRenderer={this._renderPosHeader}/>
    );
  },

  _getDescColumn: function() {
    return (
      <Column
        label="Description"
        key={this.ENTRY.DESC.field}
        width={150}
        flexGrow={1}
        dataKey={0}
        cellRenderer={this._renderDescCell}
        headerRenderer={this._renderDescHeader}/>
    );
  },

  _getTransCountColumn: function() {
    return (
      <Column
        label="Translations"
        key={this.ENTRY.TRANS_COUNT.field}
        width={120}
        cellClassName="tac"
        dataKey={0}
        cellRenderer={this._renderTransCountCell}
        headerRenderer={this._renderTransCountHeader}/>
    );
  },

  _getActionColumn: function() {
    return (
      <Column
        label=""
        key="Actions"
        cellClassName="ph1/4"
        width={300}
        dataKey={0}
        isResizable={false}
        cellRenderer={this._renderActionCell}/>
    )
  },

  _onRowMouseEnter: function (event, rowIndex) {
    if (this.state.hoveredRow !== rowIndex) {
      this.setState({hoveredRow: rowIndex});
    }
  },

  _onRowMouseLeave: function () {
    if (this.state.hoveredRow !== this.NO_ROW) {
      this.setState({hoveredRow: this.NO_ROW});
    }
  },

  _onRowClick: function (event, rowIndex) {
    var id = this._rowGetter(rowIndex)[this.CONTENT_HASH_INDEX];
    if(this.props.focusedRow.rowIndex !== rowIndex) {
      Actions.updateFocusedRow(id, rowIndex);
    }
  },

  _rowClassNameGetter: function (rowIndex) {
    if(this.props.focusedRow && this.props.focusedRow.rowIndex === rowIndex) {
      return 'bgcsec30a cdtrigger';
    } else if(this.state.hoveredRow === rowIndex) {
      return 'bgcsec20a cdtrigger';
    }
  },

  _getGlossaryEntry: function (id) {
    return this.props.glossaryData[id];
  },

  /**
   * returns id in list for glossary entry.
   * Used for fixed-data-table when loading each row. See {@link _getGlossaryEntry}
   * @param rowIndex
   * @returns [id] - id in list
   */
  _rowGetter: function(rowIndex) {
    var row = this.props.glossaryIds[rowIndex];
    if(_.isUndefined(row) || row === null) {
      if(this.loadTimeout !== null) {
        clearTimeout(this.loadTimeout);
      }
      this.loadTimeout = setTimeout(() => {
        Actions.loadGlossary(rowIndex);
      }, this.TIMEOUT);
      return [null];
    } else {
      return row;
    }
  },

  render: function() {
    const columns = [
      this._getSourceColumn(),
      this._isTranslationSelected() ? this._getTransColumn() : this._getTransCountColumn(),
      this._getPosColumn(),
      this._getDescColumn(),
      this._getActionColumn()
    ]
    const termTable = (
      <Table
        onRowClick={this._onRowClick}
        onRowMouseEnter={this._onRowMouseEnter}
        onRowMouseLeave={this._onRowMouseLeave}
        rowClassNameGetter={this._rowClassNameGetter}
        rowHeight={this.CELL_HEIGHT}
        rowGetter={this._rowGetter}
        rowsCount={this.props.totalCount}
        width={this.state.tbl_width}
        height={this.state.tbl_height}
        headerHeight={this.CELL_HEIGHT}>
        {columns}
      </Table>
    )
    const addTerms = this.props.allowNewEntry ? (
      <span className='ml1/4 difx aic'> Add a <NewEntryModal className='mh1/4' srcLocale={this.props.srcLocale}/> or <ImportModal className='ml1/4' srcLocale={this.props.srcLocale} transLocales={this.props.locales}/>.</span>
    ) : null
    const noResultsState = this.props.filter && !this.props.totalCount && !this.props.loading ? (
      <div className='posa a0 mt2 df aic jcc'>
        <p className='csec50 df aic'>
          <Icon name='info' size='1' className='mr1/4'/>
          No results for "{this.props.filter}". Maybe try another search.
        </p>
      </div>
    ) : null
    const emptyState = !this.props.filter && !this.props.totalCount && !this.props.loading ? (
      <div className='posa a0 mt2 df aic jcc'>
        <p className='csec50 df aic'>
          <Icon name='info' size='1' className='mr1/4'/>
          No terms have been entered. {addTerms}
        </p>
      </div>
    ) : null
    return (
      <div className='posr'>
        {termTable}
        {noResultsState}
        {emptyState}
      </div>
    )
  }
});

export default DataTable;
