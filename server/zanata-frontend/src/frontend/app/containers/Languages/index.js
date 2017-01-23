import React, {PropTypes, Component} from 'react'
import {connect} from 'react-redux'
import {
  Button, InputGroup, FormGroup, FormControl,
  Badge, Pagination
}
  from 'react-bootstrap'
import Helmet from 'react-helmet'
import {debounce, find} from 'lodash'
import Entry from './Entry'
import NewLanguageModal from './NewLanguageModal'
import {Icon, Notification, LoaderText} from '../../components'

import {
  initialLoad,
  handleDelete,
  handleUpdatePageSize,
  handleUpdateSort,
  handleUpdateSearch,
  handlePageUpdate,
  handleNewLanguageDisplay,
  pageSizeOption,
  sortOption
} from '../../actions/languages'

class Languages extends Component {
  componentDidMount () {
    this.props.handleInitLoad()
  }

  render () {
    const {
      searchText,
      size,
      sort,
      page,
      results,
      totalCount,
      permission,
      user,
      loading,
      deleting,
      notification,
      handleDelete,
      handleOnUpdatePageSize,
      handleOnUpdateSort,
      handleOnUpdateSearch,
      handlePageChanged,
      handleOnDisplayNewLanguage
    } = this.props

    const totalPage = Math.floor(totalCount / size) +
      (totalCount % size > 0 ? 1 : 0)

    return (
      <div className='page wide-view-theme'>
        {notification &&
        (<Notification severity={notification.severity}
          message={notification.message}
          details={notification.details}
          show={!!notification} />
        )
        }
        <Helmet title='Languages' />
        <div className='center-block'>
          <div className='clearfix'
            id='languages-form'>
            <h2>
              Languages {!loading &&
                <Badge className='default'>{totalCount}</Badge>}
            </h2>
            {permission.canAddLocale &&
              <div>
                <Button small bsStyle='primary'
                  id='btn-language-add-new'
                  onClick={handleOnDisplayNewLanguage}>
                  <Icon name='plus' className='s0 plusicon'
                    title='plus' />
                  Add new language
                </Button>
                <NewLanguageModal />
              </div>
            }
            <div className='toolbar col-xs-12'>
              <FormGroup className='search-box col-xs-6 col-sm-5 col-md-6
                col-lg-7'>
                <InputGroup>
                  <FormControl type='text'
                    defaultValue={searchText}
                    onChange={handleOnUpdateSearch} />
                  <InputGroup.Addon>
                    <Icon name='search'
                      className='s1'
                      title='search' />
                  </InputGroup.Addon>
                </InputGroup>
              </FormGroup>

              {loading
                ? <div className='content-view-container'>
                  <span className='list-inline'>
                    <LoaderText loading />
                  </span>
                </div>
                : (<div>
                  <div className='sort-items
                    col-xs-6 col-sm-4 col-md-4 col-lg-3'>
                    <FormControl componentClass='select'
                      className='pull-right' id='sort-options'
                      onChange={handleOnUpdateSort} value={sort.value}>
                    {sortOption.map(function (sort, i) {
                      return <option key={i} value={sort.value}>
                        {sort.display}</option>
                    })}
                    </FormControl>
                  </div>
                  <div className='show-items pull-right col-xs-5
                    col-sm-3 col-md-2 col-lg-2'>
                    <span>Show</span>
                    <FormControl inline componentClass='select'
                      onChange={handleOnUpdatePageSize} value={size}
                      id='page-size-options'>
                    {pageSizeOption.map(function (value, i) {
                      return <option key={i} value={value}>
                        {value}</option>
                    })}
                    </FormControl>
                  </div>
                  <div className='page-count col-xs-7 col-sm-8
                    col-md-12'>
                    <Pagination
                      prev
                      next
                      bsSize='medium'
                      items={totalPage}
                      activePage={page}
                      onSelect={handlePageChanged} />
                  </div>
                </div>)
              }
            </div>
            {!loading &&
              <div className='left-form tablepadding col-xs-12'>
                <table className='table' id='languages-table'>
                  <thead>
                    <tr className='hidden'>
                      <th>Language</th>
                      <th>&nbsp;</th>
                      <th>&nbsp;</th>
                    </tr>
                  </thead>
                  <tbody>
                    {results.map(function (value, i) {
                      return <Entry key={i} locale={value}
                        userLanguageTeams={user.languageTeams}
                        permission={permission}
                        handleDelete={handleDelete}
                        isDeleting={deleting} />
                    })}
                  </tbody>
                </table>
              </div>}
          </div>
        </div>
      </div>
    )
  }
}

Languages.propTypes = {
  page: PropTypes.number,
  notification: PropTypes.object,
  permission: PropTypes.object,
  user: PropTypes.object,
  searchText: PropTypes.string,
  size: PropTypes.number,
  sort: PropTypes.object,
  results: PropTypes.array,
  totalCount: PropTypes.number,
  loading: PropTypes.bool,
  deleting: PropTypes.bool,
  handleInitLoad: PropTypes.func,
  handleDelete: PropTypes.func,
  handleOnUpdatePageSize: PropTypes.func,
  handleOnUpdateSort: PropTypes.func,
  handleOnUpdateSearch: PropTypes.func,
  handlePageChanged: PropTypes.func,
  handleOnDisplayNewLanguage: PropTypes.func
}

const mapStateToProps = (state) => {
  let urlSort = state.routing.location.query.sort
  if (urlSort) {
    urlSort = find(sortOption, function (sort) {
      return sort.value === urlSort
    })
    if (!urlSort) {
      urlSort = sortOption[0]
    }
  } else {
    urlSort = sortOption[0]
  }

  const {
    locales,
    loading,
    permission,
    user,
    notification,
    deleting
  } = state.languages
  return {
    searchText: state.routing.location.query.search || '',
    page: parseInt(state.routing.location.query.page) || 1,
    size: parseInt(state.routing.location.query.size) || pageSizeOption[0],
    sort: urlSort,
    results: locales.results,
    totalCount: locales.totalCount,
    loading,
    permission,
    user,
    notification,
    deleting
  }
}

const mapDispatchToProps = (dispatch) => {
  const updateSearch = debounce((val) =>
    dispatch(handleUpdateSearch(val)), 200)

  return {
    handleInitLoad: () => {
      dispatch(initialLoad())
    },
    handleDelete: (localeId) => {
      dispatch(handleDelete(localeId))
    },
    handleOnUpdatePageSize: (event) => {
      dispatch(handleUpdatePageSize(event.target.value || ''))
    },
    handleOnUpdateSort: (event) => {
      dispatch(handleUpdateSort(event.target.value || ''))
    },
    handleOnUpdateSearch: (event) => {
      updateSearch(event.target.value || '')
    },
    handlePageChanged: (page) => {
      dispatch(handlePageUpdate(page))
    },
    handleOnDisplayNewLanguage: () => {
      dispatch(handleNewLanguageDisplay(true))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Languages)
