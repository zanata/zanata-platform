// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {connect} from 'react-redux'
import {debounce, isEmpty} from 'lodash'
import {
  Icon,
  TextInput,
  Link,
  Select
} from '../../components'
import Header from './Header'
import {Button, Row} from 'react-bootstrap'
import {
  glossaryChangeLocale,
  glossaryFilterTextChanged,
  glossarySortColumn,
  glossaryToggleImportFileDisplay,
  glossaryToggleExportFileDisplay,
  glossaryToggleNewEntryModal,
  glossaryToggleDeleteAllEntriesModal,
  glossaryDeleteAll
} from '../../actions/glossary-actions'
import ImportModal from './ImportModal'
import ExportModal from './ExportModal'
import NewEntryModal from './NewEntryModal'
import DeleteAllEntriesModal from './DeleteAllEntriesModal'
import {getProjectUrl} from '../../utils/UrlHelper'

/**
 * Header for glossary page
 */
class ViewHeader extends Component {
  static propTypes = {
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

  currentLocaleCount = () => {
    if (this.props.filterText && this.props.results) {
      return this.props.results
        .filter(result => result.glossaryTerms.length >= 2).length
    } else {
      const selectedTransLocaleObj = this.props.transLocales
        .find((locale) => locale.value === this.props.selectedTransLocale)
      return selectedTransLocaleObj ? selectedTransLocaleObj.count : 0
    }
  }

  localeOptionsRenderer = (op) => {
    return (
      <span className='localeOptions'>
        <span className='localeOptions-label' title={op.label}>
          {op.label}
        </span>
        <span className='localeOptions-value'>
          {op.value}
        </span>
        <span className='localeOptions-count'>
          {op.count}
        </span>
      </span>
    )
  }

  handleClearSearch = () => {
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
        <Icon name='locked' className='s1' parentClassName='iconLocked' />
      </span>)
    const showDeleteAll = permission.canDeleteEntry && !isEmptyTerms

    const projectUrl = project && getProjectUrl(project)

    const projectLink = project && (
      <div className='projectLink'>
        <Link icon='project' link={projectUrl} useHref>
          <Row>
            <Icon name='project' className='s1' parentClassName='iconProject' />
            <span className='hidden-lesm'>{project.name}</span>
          </Row>
        </Link>
      </div>
      )

    /* eslint-disable max-len, react/jsx-no-bind, no-return-assign */
    return (
      <Header className='header-glossary' title={title} icon={icon}
        extraHeadingElements={projectLink}
        extraElements={(
          <div className='u-flexRowCenter'>
            <TextInput
              className='textInput glossarySearch'
              ref={(ref) => this.searchInput = ref}
              type='search'
              placeholder='Search Terms…'
              accessibilityLabel='Search Terms'
              defaultValue={filterText}
              onChange={handleFilterFieldUpdate} />
            <Button bsStyle='link'
              title='Cancel search'
              disabled={isEmpty(filterText)}
              onClick={this.handleClearSearch}>
              <Icon name='cross' className='s1'
                parentClassName='iconCross-glossary' />
            </Button>
            <div className='glossaryButtons'>
                  {permission.canAddNewEntry && (
                    <div className='glossaryBtn topBtn'>
                      <Button bsStyle='link' type='button'
                        onClick={() => handleImportFileDisplay(true)}>
                        <Row>
                          <Icon name='import' className='s1'
                            parentClassName='iconImport' />
                          <span className='hidden-lesm'>Import</span>
                        </Row>
                      </Button>
                      <ImportModal />
                    </div>)}

                  {permission.canDownload && !isEmptyTerms && (
                    <div className='glossaryBtn topBtn'>
                      <Button bsStyle='link' type='button'
                        onClick={() => handleExportFileDisplay(true)}>
                        <Row>
                          <Icon name='export' className='s1'
                            parentClassName='iconExport' />
                          <span className='hidden-lesm'>Export</span>
                        </Row>
                      </Button>
                      <ExportModal />
                    </div>)}

                  {permission.canAddNewEntry && (
                    <div className='glossaryBtn topBtn'>
                      <Button bsStyle='link' onClick={() =>
                        handleNewEntryDisplay(true)}>
                        <Row>
                          <Icon name='plus' className='s1' parentClassName='iconPlus2' />
                          <span className='hidden-lesm'>New</span>
                        </Row>
                      </Button>
                      <NewEntryModal />
                    </div>)}

                  {showDeleteAll && (
                    <div className='glossaryBtn topBtn'>
                      <DeleteAllEntriesModal show={deleteAll.show}
                        isDeleting={deleteAll.isDeleting}
                        handleDeleteAllEntriesDisplay={
                          handleDeleteAllEntriesDisplay}
                        handleDeleteAllEntries={handleDeleteAllEntries} />
                    </div>)}
            </div>
          </div>
        )}>
        <div className='glossaryTable'>
          <table>
            <tbody>
              <tr className='tr-flex1'>
                <td className='td-3'
                  onClick={() => handleSortColumn('src_content')}>
                  <Button bsStyle='link' type='button'>
                    <Row>
                      {'src_content' in sort
                        ? (sort.src_content === true)
                          ? <Icon name='chevron-down' className='s1' />
                          : <Icon name='chevron-up' className='s1' />
                        : ''}
                      <Icon name='glossary' className='s1'
                        parentClassName='iconGlossary-neutral' />
                      <span>
                      English (United States)
                      </span>
                      <span className='u-textMutedLeft'>{termCount}</span>
                    </Row>
                  </Button>
                </td>
                <td
                  className='languageSelect td-3'>
                  <Select
                    name='language-selection'
                    placeholder={statsLoading
                      ? 'Loading…' : 'Select a language…'}
                    className='inputFlex'
                    isLoading={statsLoading}
                    value={selectedTransLocale}
                    options={transLocales}
                    pageSize={20}
                    optionRenderer={this.localeOptionsRenderer}
                    onChange={handleTranslationLocaleChange}
                  />
                  {selectedTransLocale &&
                  (<span className='hidden-xs'>
                    <Row>
                      <Icon name='translate' className='s1' parentClassName='iconTranslate-neutral' />
                      <span className='u-textNeutral'>
                      {currentLocaleCount}
                      </span>
                    </Row>
                  </span>
                  )}
                </td>
                <td className='hidesmall td-1'
                  onClick={() => handleSortColumn('part_of_speech')}>
                  <Button bsStyle='link' type='button'>
                    <Row>
                      {'part_of_speech' in sort
                        ? (sort.part_of_speech === true)
                          ? <Icon name='chevron-down'
                            className='s1' parentClassName='iconChevron' />
                          : <Icon name='chevron-up'
                            className='s1' parentClassName='iconChevron' />
                        : ''}
                      <span className='u-marginL--rq'>
                      Part of Speech
                      </span>
                    </Row>
                  </Button>
                </td>
                <td className='td-1' />
              </tr>
            </tbody>
          </table>
        </div>
      </Header>
    /* eslint-enable max-len, react/jsx-no-bind, no-return-assign */
    )
  }
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
  // FIXME probably out of date, needs the one that was passed as props
  const query = state.routing.locationBeforeTransitions.query
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
