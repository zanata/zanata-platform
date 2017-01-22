import React, { Component, PropTypes } from 'react'
import Helmet from 'react-helmet'
import { connect } from 'react-redux'
import { isEmpty, debounce } from 'lodash'
import { Button }
  from 'react-bootstrap'
import TeaserList from './TeaserList'
import { TextInput, Icon } from '../../components'
import {
  searchTextChanged,
  searchPageInitialLoad,
  updateSearchPage,
  SIZE_PER_PAGE
} from '../../actions/explore'

/**
 * Root component for Explore page
 */
class Explore extends Component {
  componentDidMount () {
    this.props.handleInitLoad()
  }

  handleKeyDown (e) {
    if (e.key === 'Escape') {
      this.handleClearSearch()
    }
  }

  handleClearSearch () {
    if (this.searchInput !== null) {
      this.searchInput._onClear()
    }
    this.props.handleSearchCancelClick()
  }

  render () {
    const {
      handleSearchTextChange,
      handleUpdateSearchPage,
      searchText,
      projectPage,
      groupPage,
      personPage,
      languageTeamPage,
      searchResults,
      searchError,
      searchLoading
    } = this.props
    let content
    if (searchError) {
      content = (<p>Error searching for '{searchText}'.<br />
        {searchResults.message}. Please try again.</p>)
    } else {
      const projectContent = (<TeaserList
        loading={searchLoading['Project'] === true}
        items={searchResults['Project']
          ? searchResults['Project'].results : []}
        title='Projects'
        totalCount={searchResults['Project']
          ? parseInt(searchResults['Project'].totalCount) : 0}
        type='Project'
        key='Project'
        sizePerPage={SIZE_PER_PAGE}
        updatePage={handleUpdateSearchPage}
        page={projectPage} />)

      const groupContent = (<TeaserList
        loading={isEmpty(searchResults) && searchLoading['Group']}
        items={searchResults['Group']
          ? searchResults['Group'].results : []}
        title='Groups'
        totalCount={searchResults['Group']
          ? parseInt(searchResults['Group'].totalCount) : 0}
        type='Group'
        key='Group'
        sizePerPage={SIZE_PER_PAGE}
        updatePage={handleUpdateSearchPage}
        page={groupPage} />)
      const personContent = searchText &&
        (<TeaserList
          loading={searchLoading['Person'] === true}
          items={searchResults['Person']
            ? searchResults['Person'].results : []}
          title='People'
          totalCount={searchResults['Person']
            ? parseInt(searchResults['Person'].totalCount) : 0}
          type='Person'
          key='Person'
          sizePerPage={SIZE_PER_PAGE}
          updatePage={handleUpdateSearchPage}
          page={personPage} />)

      const languageTeamContent = searchText &&
        (<TeaserList
          loading={searchLoading['LanguageTeam'] === true}
          items={searchResults['LanguageTeam']
            ? searchResults['LanguageTeam'].results : []}
          title='Language Teams'
          totalCount={searchResults['LanguageTeam']
            ? parseInt(searchResults['LanguageTeam'].totalCount) : 0}
          type='LanguageTeam'
          key='LanguageTeam'
          sizePerPage={SIZE_PER_PAGE}
          updatePage={handleUpdateSearchPage}
          page={languageTeamPage} />)

      content = (
        <div>
          {projectContent}
          {personContent}
          {languageTeamContent}
          {groupContent}
        </div>)
    }
    /* eslint-disable react/jsx-no-bind, no-return-assign */
    return (
      <div className='page scroll-view-theme' id='explore'>
        <Helmet title='Search' />
        <div className='header-classes'>
          <h1 className='hidden' level='1'>Search</h1>
          <div className='search-view-theme'>
            <Icon name='search' className='s1 list-inline' />
            <TextInput
              maxLength={100}
              ref={(ref) => this.searchInput = ref}
              id='explore_search'
              type='search'
              className='textInput'
              placeholder='Search Zanata…'
              accessibilityLabel='Search Zanata'
              defaultValue={searchText}
              onKeyDown={(e) => { this.handleKeyDown(e) }}
              onChange={handleSearchTextChange}
            />
            <Button
              bsStyle='link' disabled={isEmpty(searchText)}
              onClick={(e) => { this.handleClearSearch() }}>
              Cancel
            </Button>
          </div>
        </div>
        <div className='content-view-container'>
            {content}
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}

Explore.propTypes = {
  location: PropTypes.object,
  searchText: PropTypes.string,
  projectPage: PropTypes.number,
  groupPage: PropTypes.number,
  personPage: PropTypes.number,
  languageTeamPage: PropTypes.number,
  searchResults: PropTypes.object,
  searchError: PropTypes.bool,
  searchLoading: PropTypes.shape({
    Project: PropTypes.bool,
    LanguageTeam: PropTypes.bool,
    Person: PropTypes.bool,
    Group: PropTypes.bool
  }),
  handleInitLoad: PropTypes.func,
  handleSearchCancelClick: PropTypes.func,
  handleSearchTextChange: PropTypes.func,
  handleUpdateSearchPage: PropTypes.func
}

const mapStateToProps = (state) => {
  return {
    location: state.routing.location,
    searchText: state.routing.location.query.q,
    projectPage: parseInt(state.routing.location.query.projectPage),
    groupPage: parseInt(state.routing.location.query.groupPage),
    personPage: parseInt(state.routing.location.query.personPage),
    languageTeamPage: parseInt(state.routing.location.query.languageTeamPage),
    searchResults: state.explore.results,
    searchError: state.explore.error,
    searchLoading: state.explore.loading
  }
}

const mapDispatchToProps = (dispatch) => {
  const updateSearchQuery = debounce((val) =>
    dispatch(searchTextChanged(val)), 250)

  return {
    handleSearchCancelClick: () => {
      dispatch(searchTextChanged(''))
    },
    handleSearchTextChange: (event) => {
      updateSearchQuery(event.target.value || '')
    },
    handleInitLoad: () => {
      dispatch(searchPageInitialLoad())
    },
    handleUpdateSearchPage: (type, currentPage, totalPage, next) => {
      dispatch(updateSearchPage(type, currentPage, totalPage, next))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Explore)
