import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { debounce, isEmpty } from 'lodash'
import {
  Icon,
  Row,
  Select,
  TableCell,
  TableRow,
  TextInput,
  View,
  Link
} from 'zanata-ui'
import Header from './Header'
import { Button } from 'react-bootstrap'
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
import { getProjectUrl } from '../../utils/UrlHelper'

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
      title,
      project,
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
    const isEmptyTerms = termCount <= 0
    const currentLocaleCount = this.currentLocaleCount()
    const isReadOnly = !(permission.canAddNewEntry ||
      permission.canUpdateEntry || permission.canDeleteEntry)
    const icon = isReadOnly && (
      <span title='read-only'>
        <Icon name='locked' atomic={{m: 'Mend(re)', c: 'C(warning)'}} />
      </span>)
    const showDeleteAll = permission.canDeleteEntry && !isEmptyTerms

    const projectUrl = project && getProjectUrl(project)

    const projectLink = project && (
      <div className='D(ib) Mstart(rh)'>
        <Link icon='project' link={projectUrl} useHref>
          <Row>
            <Icon name='project' atomic={{m: 'Mend(re)'}} />
            <span className='Hidden--lesm'>{project.name}</span>
          </Row>
        </Link>
      </div>
    )

    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <Header title={title} icon={icon}
        extraHeadingElements={projectLink}
        extraElements={(
          <View theme={{base: { ai: 'Ai(c)', fld: '' }}}>
            <TextInput
              className='textInput'
              ref={(ref) => this.searchInput = ref}
              theme={{base: { flx: 'Flx(flx1)', m: 'Mstart(rh)--md' }}}
              type='search'
              placeholder='Search Terms…'
              accessibilityLabel='Search Terms'
              defaultValue={filterText}
              onChange={handleFilterFieldUpdate} />
            <Button bsStyle='link'
              title='Cancel search'
              disabled={isEmpty(filterText)}
              onClick={(e) => { this.handleClearSearch() }}>
              <Icon name='cross' />
            </Button>

            {permission.canAddNewEntry && (
              <div className='Mstart(rh)--md Mstart(rq)'>
                <Button bsStyle='link' type='button'
                  onClick={() => handleImportFileDisplay(true)}>
                  <Row>
                    <Icon name='import' atomic={{m: 'Mend(re)'}} />
                    <span className='Hidden--lesm'>Import</span>
                  </Row>
                </Button>
                <ImportModal />
              </div>)}

            {permission.canDownload && !isEmptyTerms && (
              <div className='Mstart(rh)--md Mstart(rq)'>
                <Button bsStyle='link' type='button'
                  onClick={() => handleExportFileDisplay(true)}>
                  <Row>
                    <Icon name='export' atomic={{m: 'Mend(re)'}} />
                    <span className='Hidden--lesm'>Export</span>
                  </Row>
                </Button>
                <ExportModal />
              </div>)}

             {permission.canAddNewEntry && (
               <div className='Mstart(rh)--md Mstart(rq)'>
                 <Button bsStyle='link' onClick={() =>
                   handleNewEntryDisplay(true)}>
                   <Row>
                     <Icon name='plus' atomic={{m: 'Mend(re)'}} />
                     <span className='Hidden--lesm'>New</span>
                   </Row>
                 </Button>
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
          }}}>
          <TableRow
            theme={{ base: { bd: '' } }}
            className='Flx(flx1)'>
            <TableCell size='3'
              onClick={() => handleSortColumn('src_content')}>
              <Button bsStyle='link' type='button'>
                <Row>
                  {'src_content' in sort
                    ? (sort.src_content === true)
                    ? <Icon name='chevron-down' />
                    : <Icon name='chevron-up' />
                    : ''}
                  <Icon name='glossary'
                    atomic={{c: 'C(neutral)', m: 'Mend(re) MStart(rq)'}} />
                  <span>
                    English (United States)
                  </span>
                  <span className='C(muted) Mstart(rq)'>{termCount}</span>
                </Row>
              </Button>
            </TableCell>
            <TableCell
              className='langSelect'
              theme={{base: {lineClamp: ''}}}>
              <Select
                name='language-selection'
                placeholder={statsLoading
                      ? 'Loading…' : 'Select a language…'}
                className='Flxg(1)'
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
            <TableCell hideSmall size='3'
              onClick={() => handleSortColumn('part_of_speech')}>
              <Button bsStyle='link' type='button'>
                <Row>
                  {'part_of_speech' in sort
                    ? (sort.part_of_speech === true)
                    ? <Icon name='chevron-down' />
                    : <Icon name='chevron-up' />
                    : ''}
                  <span className='MStart(rq)'>
                    Part of Speech
                  </span>
                </Row>
              </Button>
            </TableCell>
            <TableCell size='3' />
          </TableRow>
        </View>
      </Header>
    )
  }
}

ViewHeader.propTypes = {
  title: PropTypes.string,
  project: PropTypes.object,
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
    deleteAll,
    project
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
    deleteAll,
    project
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
