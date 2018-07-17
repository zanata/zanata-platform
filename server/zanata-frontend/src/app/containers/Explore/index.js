// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Helmet from 'react-helmet'
import { connect } from 'react-redux'
import { isEmpty, debounce } from 'lodash'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import TeaserList from './TeaserList'
import { TextInput } from '../../components'
import {
  searchTextChanged,
  searchPageInitialLoad,
  updateSearchPage,
  SIZE_PER_PAGE
} from '../../actions/explore-actions'

/**
 * Root component for Explore page
 */
class Explore extends Component {
  static propTypes = {
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

  componentDidMount () {
    this.props.handleInitLoad()
  }

  handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      this.handleClearSearch()
    }
  }

  handleClearSearch = () => {
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
      <div className='scrollView' id='explore'>
        <Helmet title='Search' />
        <div className='mb5'>
          <h1 className='dn'>Search</h1>
          <div className='searchView'>
            <Icon type='search' className='s0 v-mid' />
            <TextInput
              maxLength={100}
              ref={(ref) => this.searchInput = ref}
              id='explore_search'
              type='search'
              className='textInput'
              placeholder='Search Zanata…'
              accessibilityLabel='Search Zanata'
              defaultValue={searchText}
              onKeyDown={this.handleKeyDown}
              onChange={handleSearchTextChange}
            />
            <Button
              className='btn-link' disabled={isEmpty(searchText)}
              onClick={this.handleClearSearch} aria-label='button'>
              Cancel
            </Button>
          </div>
        </div>
        <div className='containerContentView'>
            {content}
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}

const mapStateToProps = (state, { location }) => {
  const {
    groupPage, languageTeamPage, personPage, projectPage, q } = location.query
  return {
    location,
    searchText: q,
    projectPage: parseInt(projectPage),
    groupPage: parseInt(groupPage),
    personPage: parseInt(personPage),
    languageTeamPage: parseInt(languageTeamPage),
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
