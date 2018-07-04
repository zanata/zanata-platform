// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {connect} from 'react-redux'
import Helmet from 'react-helmet'
import { debounce, find, isEmpty } from 'lodash'
import Entry from './Entry'
import NewLanguageModal from './NewLanguageModal'
import {LoaderText} from '../../components'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Layout from 'antd/lib/layout'
import 'antd/lib/layout/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import Notification from 'antd/lib/notification'
import 'antd/lib/notification/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Pagination from 'antd/lib/pagination'
import 'antd/lib/pagination/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'

const Search = Input.Search
const Option = Select.Option

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
} from '../../actions/languages-actions'

class Languages extends Component {
  static propTypes = {
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

  constructor (props) {
    super(props)
    this.state = {
      searchText: props.searchText
    }
    // Debounce works when you call it once, then use the returned function
    // multiple times.
    // Do not extract this to a function like `() => { debounce(...) }` since
    // that would make a new debounced function instance on every call.
    this.handleUpdateSearch = debounce(props.handleOnUpdateSearch, 200)
  }

  componentDidMount () {
    this.props.handleInitLoad()
  }

  componentDidUpdate (prevProps) {
    const { notification } = this.props
    if (notification && prevProps.notification !== notification) {
      Notification[notification.severity]({
        message: notification.message,
        description: notification.description,
        duration: null
      })
    }
  }

  resetSearchText = (localeId) => {
    this.setState({
      searchText: ''
    })
    this.props.handleDelete(localeId)
  }

  onUpdateSearch = (event) => {
    const searchText = event.target.value || ''
    this.setState({
      searchText
    })
    this.handleUpdateSearch(searchText)
  }

  render () {
    const {
      size,
      sort,
      page,
      results,
      totalCount,
      permission,
      user,
      loading,
      deleting,
      handleOnUpdatePageSize,
      handleOnUpdateSort,
      handlePageChanged,
      handleOnDisplayNewLanguage
    } = this.props

    const resetSearchText = this.resetSearchText

    const totalPage = Math.floor(totalCount / size) +
      (totalCount % size > 0 ? 1 : 0)

    const noResults = isEmpty(results)

    /* eslint-disable max-len, react/jsx-no-bind */
    return (
      <div className='languages'>
        <Layout>
          <Helmet title='Languages' />
          <div id='languages-form'>
            <h1>
              Languages {!loading &&
                <Tag color='03A6D7'>{totalCount}</Tag>
              }
            </h1>
            {permission.canAddLocale &&
              <div className='ml3'>
                <Button type="primary" icon="plus"
                  id="btn-language-add-new"
                  aria-label="button"
                  onClick={handleOnDisplayNewLanguage}>
                Add new language</Button>
                <NewLanguageModal />
              </div>
            }
            <div className='mt3 mb3'>
              {loading
                ? <div>
                  <span className='di'>
                    <LoaderText loading />
                  </span>
                </div>
                : (
                <span className='w-100 mt2'>
                  <Col xs={24} sm={23} md={8} className='ml3'>
                    <span>
                      <Search
                        value={this.state.searchText}
                        onSearch={this.onUpdateSearch}
                        enterButton />
                    </span>
                  </Col>
                  <Col xs={24} sm={12} md={6} className='ml3 mb2'>
                    <Select className='w-100'
                      id='sort-options'
                      onChange={handleOnUpdateSort} value={sort.value}>
                      {sortOption.map(function (sort, i) {
                        return <Option key={i} value={sort.value}>
                        {sort.display}</Option>
                      })}
                    </Select>
                  </Col>
                  <Col xs={24} sm={6} md={4} className='ml3'>
                    <span className='mr1'>Show</span>
                    <Select
                      onChange={handleOnUpdatePageSize} value={size}
                      id='page-size-options'>
                      {pageSizeOption.map(function (value, i) {
                        return <Option key={i} value={value}>
                        {value}</Option>
                      })}
                    </Select>
                  </Col>
                  <Col className='fr mr3 mb4'>
                    <Pagination
                      total={totalPage}
                      defaultCurrent={page}
                      onChange={handlePageChanged} />
                  </Col>
                </span>)}
                {noResults &&
                  <div className='loader-loadingContainer'>
                    <p className='tc txt-muted f4'>
                      <Icon type='global' /> No results
                    </p>
                  </div>
                }
                {!loading && !noResults &&
                  <div>
                    <table className='table' id='languages-table'>
                      <thead>
                        <tr className='dn mt4'>
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
                           handleDelete={resetSearchText}
                           isDeleting={deleting} />
                       })}
                      </tbody>
                    </table>
                  </div>}
            </div>
          </div>
        </Layout>
      </div>)
      /* eslint-enable max-len, react/jsx-no-bind */
  }
}

const mapStateToProps = (state, { location }) => {
  let urlSort = location.query.sort
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
    searchText: location.query.search || '',
    page: parseInt(location.query.page) || 1,
    size: parseInt(location.query.size) || pageSizeOption[0],
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
    handleOnUpdateSearch: (val) => {
      dispatch(handleUpdateSearch(val))
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
