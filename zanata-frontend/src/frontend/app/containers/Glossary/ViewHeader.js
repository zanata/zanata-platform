import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { debounce, isEmpty } from 'lodash'
import {
  ButtonLink,
  Icon,
  Row,
  Select,
  TableCell,
  TableRow,
  TextInput,
  View
} from 'zanata-ui'
import Header from './Header'
import {
  glossaryChangeLocale,
  glossaryFilterTextChanged,
  glossarySortColumn,
  glossaryToggleImportFileDisplay,
  glossaryToggleExportFileDisplay,
  glossaryToggleNewEntryModal,
  glossaryToggleDeleteAllEntriesModal,
  glossaryDeleteAll
} from '../../actions/glossary'
import ImportModal from './ImportModal'
import ExportModal from './ExportModal'
import NewEntryModal from './NewEntryModal'
import DeleteAllEntriesModal from './DeleteAllEntriesModal'

/**
 * Header for glossary page
 */
class ViewHeader extends Component {
  currentLocaleCount () {
    if (this.props.filterText && this.props.results) {
      return this.props.results
        .filter(result => result.glossaryTerms.length >= 2).length
    } else {
      const selectedTransLocaleObj = this.props.transLocales
        .find((locale) => locale.value === this.props.selectedTransLocale)
      return selectedTransLocaleObj ? selectedTransLocaleObj.count : 0
    }
  }

  localeOptionsRenderer (op) {
    return (
      <span className='D(f) Ai(c) Jc(sb)'>
        <span className='Flx(flx1) LineClamp(1)' title={op.label}>
          {op.label}
        </span>
        <span className='Flx(n) Pstart(re) Ta(end) Maw(r4) LineClamp(1)'>
          {op.value}
        </span>
        <span className='Flx(n) C(muted) Pstart(re) Ta(end) LineClamp(1) W(r2)'>
          {op.count}
        </span>
      </span>
    )
  }

  handleClearSearch () {
    if (this.searchInput !== null) {
      this.searchInput._onClear()
    }
    this.props.handleSearchCancelClick()
  }
  render () {
    const {
      filterText = '',
      termCount,
      statsLoading,
      transLocales,
      selectedTransLocale,
      handleTranslationLocaleChange,
      handleFilterFieldUpdate,
      handleImportFileDisplay,
      handleExportFileDisplay,
      handleNewEntryDisplay,
      handleDeleteAllEntriesDisplay,
      handleDeleteAllEntries,
      handleSortColumn,
      permission,
      sort,
      deleteAll
      } = this.props
    const currentLocaleCount = this.currentLocaleCount()
    const isReadOnly = !(permission.canAddNewEntry ||
      permission.canUpdateEntry || permission.canDeleteEntry)
    const icon = isReadOnly ? 'locked' : undefined
    const tooltip = isReadOnly ? 'read-only' : undefined
    const showDeleteAll = permission.canDeleteEntry && termCount > 0
    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <Header title='Glossary' icon={icon} tooltip={tooltip}
        extraElements={(
          <View theme={{base: { ai: 'Ai(c)', fld: '' }}}>
            <TextInput
              ref={(ref) => this.searchInput = ref}
              theme={{base: { flx: 'Flx(flx1)', m: 'Mstart(rh)--md' }}}
              type='search'
              placeholder='Search Terms…'
              accessibilityLabel='Search Terms'
              defaultValue={filterText}
              onChange={handleFilterFieldUpdate} />
            <ButtonLink
              title='Cancel search'
              disabled={isEmpty(filterText)}
              onClick={(e) => { this.handleClearSearch() }}>
              <Icon name='cross' />
            </ButtonLink>

            {permission.canAddNewEntry && (
              <div className='Mstart(rh)--md Mstart(rq)'>
                <ButtonLink type='default'
                  onClick={() => handleImportFileDisplay(true)}>
                  <Row>
                    <Icon name='import' atomic={{m: 'Mend(re)'}} />
                    <span className='Hidden--lesm'>Import</span>
                  </Row>
                </ButtonLink>
                <ImportModal />
              </div>)}

            {permission.canDownload && (
              <div className='Mstart(rh)--md Mstart(rq)'>
                <ButtonLink type='default'
                  onClick={() => handleExportFileDisplay(true)}>
                  <Row>
                    <Icon name='export' atomic={{m: 'Mend(re)'}} />
                    <span className='Hidden--lesm'>Export</span>
                  </Row>
                </ButtonLink>
                <ExportModal />
              </div>)}

             {permission.canAddNewEntry && (
               <div className='Mstart(rh)--md Mstart(rq)'>
                 <ButtonLink onClick={() => handleNewEntryDisplay(true)}>
                   <Row>
                     <Icon name='plus' atomic={{m: 'Mend(re)'}} />
                     <span className='Hidden--lesm'>New</span>
                   </Row>
                 </ButtonLink>
                 <NewEntryModal />
               </div>)}

             {showDeleteAll && (
               <div className='Mstart(rh)--md Mstart(rq)'>
                 <DeleteAllEntriesModal show={deleteAll.show}
                   isDeleting={deleteAll.isDeleting}
                   handleDeleteAllEntriesDisplay={(display) =>
                    handleDeleteAllEntriesDisplay(display)}
                   handleDeleteAllEntries={handleDeleteAllEntries} />
               </div>)}
          </View>
        )}>
        <View theme={{
          base: {
            w: 'W(100%)',
            m: 'Mt(rq) Mt(rh)--sm'
          }}}
        >
          <TableRow
            theme={{ base: { bd: '' } }}
            className='Flxg(1)'>
            <TableCell size='3'
              onClick={() => handleSortColumn('src_content')}>
              <ButtonLink type='default'>
                <Row>
                  {'src_content' in sort
                    ? (sort.src_content === true)
                      ? <Icon name='chevron-down' />
                      : <Icon name='chevron-up' />
                    : ''}
                  <Icon name='glossary'
                    atomic={{c: 'C(neutral)', m: 'Mend(re) MStart(rq)'}} />
                  <span className='LineClamp(1,24px)'>
                    English (United States)
                  </span>
                  <span className='C(muted) Mstart(rq)'>{termCount}</span>
                </Row>
              </ButtonLink>
            </TableCell>
            <TableCell tight size={'3'}
              theme={{base: {lineClamp: ''}}}>
              <Select
                name='language-selection'
                placeholder={statsLoading
                      ? 'Loading…' : 'Select a language…'}
                className='Flx(flx1)'
                isLoading={statsLoading}
                value={selectedTransLocale}
                options={transLocales}
                pageSize={20}
                optionRenderer={this.localeOptionsRenderer}
                onChange={handleTranslationLocaleChange}
              />
              {selectedTransLocale &&
              (<Row>
                <Icon name='translate'
                  atomic={{c: 'C(neutral)', m: 'Mstart(rq) Mend(re)'}} />
                <span className='C(muted)'>
                  {currentLocaleCount}
                </span>
              </Row>)}
            </TableCell>
            <TableCell hideSmall
              onClick={() => handleSortColumn('part_of_speech')}>
              <ButtonLink type='default'>
                <Row>
                  {'part_of_speech' in sort
                    ? (sort.part_of_speech === true)
                      ? <Icon name='chevron-down' />
                      : <Icon name='chevron-up' />
                    : ''}
                  <span className='LineClamp(1,24px) MStart(rq)'>
                    Part of Speech
                  </span>
                </Row>
              </ButtonLink>
            </TableCell>
            <TableCell size='2' />
          </TableRow>
        </View>
      </Header>
    )
  }
}

ViewHeader.propTypes = {
  results: PropTypes.object,
  termCount: PropTypes.number.isRequired,
  statsLoading: PropTypes.bool,
  transLocales: PropTypes.arrayOf(
    PropTypes.shape({
      count: PropTypes.number.isRequired,
      label: PropTypes.string.isRequired,
      value: PropTypes.string.isRequired
    })
  ).isRequired,
  filterText: PropTypes.string,
  selectedTransLocale: PropTypes.string,
  permission: PropTypes.shape({
    canAddNewEntry: PropTypes.bool,
    canUpdateEntry: PropTypes.bool,
    canDeleteEntry: PropTypes.bool
  }).isRequired,
  sort: PropTypes.shape({
    src_content: PropTypes.bool,
    part_of_speech: PropTypes.bool
  }).isRequired,
  deleteAll: PropTypes.object,
  handleTranslationLocaleChange: PropTypes.func,
  handleFilterFieldUpdate: PropTypes.func,
  handleImportFileDisplay: PropTypes.func,
  handleNewEntryDisplay: PropTypes.func,
  handleDeleteAllEntriesDisplay: PropTypes.func,
  handleDeleteAllEntries: PropTypes.func,
  handleSortColumn: PropTypes.func,
  handleSearchCancelClick: PropTypes.func,
  handleExportFileDisplay: PropTypes.func
}

const mapStateToProps = (state) => {
  const {
    stats,
    statsLoading,
    termCount,
    filter,
    permission,
    sort,
    deleteAll
    } = state.glossary
  const query = state.routing.location.query
  return {
    termCount,
    statsLoading,
    transLocales: stats.transLocales,
    filterText: filter,
    selectedTransLocale: query.locale,
    permission,
    sort,
    deleteAll
  }
}

const mapDispatchToProps = (dispatch) => {
  const updateFilter = debounce((val) =>
    dispatch(glossaryFilterTextChanged(val)), 200)

  return {
    handleTranslationLocaleChange: (selectedLocale) =>
      dispatch(
        glossaryChangeLocale(selectedLocale ? selectedLocale.value : '')
      ),
    handleFilterFieldUpdate: (event) => {
      updateFilter(event.target.value || '')
    },
    handleSearchCancelClick: (event) => {
      updateFilter('')
    },
    handleSortColumn: (col) => dispatch(glossarySortColumn(col)),
    handleImportFileDisplay: (display) =>
      dispatch(glossaryToggleImportFileDisplay(display)),
    handleExportFileDisplay: (display) =>
      dispatch(glossaryToggleExportFileDisplay(display)),
    handleNewEntryDisplay: (display) =>
      dispatch(glossaryToggleNewEntryModal(display)),
    handleDeleteAllEntriesDisplay: (display) =>
      dispatch(glossaryToggleDeleteAllEntriesModal(display)),
    handleDeleteAllEntries: () => dispatch(glossaryDeleteAll())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ViewHeader)
