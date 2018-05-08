// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { isUndefined, size, map } from 'lodash'
import ReactList from 'react-list'
import { Icon, LoaderText, Select, Notification } from '../../components/'
import { Row } from 'react-bootstrap'
import {
  glossaryDeleteTerm,
  glossaryResetTerm,
  glossarySelectTerm,
  glossaryUpdateField,
  glossaryUpdateTerm,
  glossaryGoFirstPage,
  glossaryGoLastPage,
  glossaryGoNextPage,
  glossaryGoPreviousPage,
  glossaryInitialLoad,
  glossaryUpdatePageSize,
  PAGE_SIZE_DEFAULT,
  PAGE_SIZE_SELECTION
} from '../../actions/glossary-actions'
import ViewHeader from './ViewHeader'
import Entry from './Entry'
import { Button, Layout } from 'antd'

/**
 * Root component for Glossary page
 */
class Glossary extends Component {
  static propTypes = {
    /**
     * Object of glossary id with term
     */
    terms: PropTypes.object,
    project: PropTypes.object,
    params: PropTypes.object,
    termIds: PropTypes.array,
    termCount: PropTypes.number,
    termsLoading: PropTypes.bool,
    transLocales: PropTypes.array,
    srcLocale: PropTypes.object,
    filterText: PropTypes.string,
    selectedTerm: PropTypes.object,
    selectedTransLocale: PropTypes.string,
    permission: PropTypes.object,
    location: PropTypes.object,
    saving: PropTypes.object,
    deleting: PropTypes.object,
    notification: PropTypes.object,
    goPreviousPage: PropTypes.func,
    goFirstPage: PropTypes.func,
    goLastPage: PropTypes.func,
    goNextPage: PropTypes.func,
    handleInitLoad: PropTypes.func,
    handleSelectTerm: PropTypes.func,
    handleTermFieldUpdate: PropTypes.func,
    handleDeleteTerm: PropTypes.func,
    handleResetTerm: PropTypes.func,
    handleUpdateTerm: PropTypes.func,
    handlePageSizeChange: PropTypes.func,
    page: PropTypes.string,
    gotoPreviousPage: PropTypes.func,
    gotoFirstPage: PropTypes.func,
    gotoLastPage: PropTypes.func,
    gotoNextPage: PropTypes.func,
    pageSize: PropTypes.string
  }

  componentDidMount () {
    const paramProjectSlug = this.props.params.projectSlug
    const projectSlug = (!paramProjectSlug || paramProjectSlug === 'undefined')
      ? undefined : paramProjectSlug

    this.props.handleInitLoad(projectSlug)
  }

  /**
   * Force component to update when changes between project glossary to glossary
   */
  componentDidUpdate (prevProps, prevState) {
    const projectSlug = this.props.params.projectSlug
    if (prevProps.params.projectSlug !== projectSlug) {
      this.props.handleInitLoad(projectSlug)
    }
  }

  /**
   * Function passed to react-list to delegate rendering of an item.
   *
   * Arguments are the overall index of the item, and the local (page?) index of
   * the item.
   *
   * This is not in the API for the latest version of react-list,which is
   * now released under a stable version number).
   *
   * Best practice is to use an arrow function here so that React will auto-bind
   * this function, but this stops working properly unless binding is done
   * inline in the JSX. Hope you have a good garbage collector.
   */
  renderItem (index, key) {
    const {
      handleSelectTerm,
      handleTermFieldUpdate,
      handleDeleteTerm,
      handleResetTerm,
      handleUpdateTerm,
      termsLoading,
      termIds,
      terms,
      selectedTransLocale,
      selectedTerm,
      permission,
      saving,
      deleting
    } = this.props
    const entryId = termIds[index]
    const selected = entryId === selectedTerm.id
    const isSaving = !isUndefined(saving[entryId])
    let entry
    if (isSaving && entryId) {
      entry = saving[entryId]
    } else if (selected) {
      entry = selectedTerm
    } else if (entryId) {
      entry = terms[entryId]
    }
    const isDeleting = !isUndefined(deleting[entryId])

    return (
      <Entry {...{
        key,
        entry,
        index,
        selected,
        isDeleting,
        isSaving,
        permission,
        selectedTransLocale,
        termsLoading,
        handleSelectTerm,
        handleTermFieldUpdate,
        handleDeleteTerm,
        handleResetTerm,
        handleUpdateTerm
      }} />
    )
  }

  render () {
    const {
      terms,
      termsLoading,
      termCount,
      notification,
      gotoPreviousPage,
      gotoFirstPage,
      gotoLastPage,
      gotoNextPage,
      page,
      pageSize,
      handlePageSizeChange,
      project
    } = this.props

    const intPageSize = pageSize ? parseInt(pageSize) : PAGE_SIZE_DEFAULT
    const totalPage = Math.floor(termCount / intPageSize) +
      (termCount % intPageSize > 0 ? 1 : 0)
    const currentPage = page ? parseInt(page) : 1
    const displayPaging = totalPage > 1
    const pageSizeOption = map(PAGE_SIZE_SELECTION, (size) => {
      return {label: size, value: size}
    })
    const headerTitle = project ? 'Project Glossary' : 'Glossary'
    let list

    /* eslint-disable react/jsx-no-bind */
    if (termsLoading) {
      list = (<div className='loader-loadingContainer'>
        <LoaderText loading loadingText='Loading' />
      </div>)
    } else if (!termsLoading && termCount) {
      list = (<ReactList
        useTranslate3d
        itemRenderer={this.renderItem.bind(this)}
        length={size(terms)}
        type='uniform'
        className='reactList'
        ref={(c) => { this.list = c }} />)
    } else {
      list = (<div className='loader-loadingContainer'>
        <span className='u-textLoadingMuted'>
          <Icon name='glossary' />
        </span>
        <p className='glossaryText-muted'>No content</p>
      </div>)
    }

    return (
      <div>
        {notification &&
          (<Notification severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
          )
        }
        <Helmet title={headerTitle} />
        <div className='wideView' id='glossary'>
          <Layout>
            <ViewHeader title={headerTitle} />
            <div className='glossaryHeader'>
              <Row>
                {termCount > 0 &&
                  <Row>
                    <span className='hidden-lesm glossaryRow'>Show</span>
                    <Select options={pageSizeOption}
                      placeholder='Terms per page'
                      value={intPageSize}
                      name='glossary-page'
                      className='glossarySelect'
                      searchable={false}
                      clearable={false}
                      onChange={handlePageSizeChange} />
                  </Row>
                }
                {displayPaging &&
                  <div className='u-pullRight glossaryPaging'>
                    <Button className='btn-link' disabled={currentPage <= 1}
                      title='First page' icon='left' aria-label='button'
                      onClick={() => {
                        gotoFirstPage(currentPage, totalPage)
                      }}
                    />
                    <Button className='btn-link' disabled={currentPage <= 1}
                      title='Previous page' icon='left' aria-label='button'
                      onClick={
                      () => {
                        gotoPreviousPage(currentPage, totalPage)
                      }}
                    />
                    <span className='u-textNeutral-top'>
                      {currentPage} of {totalPage}
                    </span>
                    <Button className='btn-link' aria-label='button'
                      disabled={currentPage === totalPage}
                      title='Next page' icon='right'
                      onClick={() => {
                        gotoNextPage(currentPage, totalPage)
                      }}
                    />
                    <Button className='btn-link' aria-label='button'
                      disabled={currentPage === totalPage}
                      title='Last page' icon='right'
                      onClick={() => {
                        gotoLastPage(currentPage, totalPage)
                      }}
                    />
                    <span className='textNeutralTotal'
                      title='Total glossary terms'>
                      <Row>
                        <Icon name='glossary' className='s1' /> {termCount}
                      </Row>
                    </span>
                  </div>
                  }
              </Row>
            </div>

            <div className='glossaryList'>
              {list}
            </div>
          </Layout>
        </div>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

const mapStateToProps = (state, { location }) => {
  const {
    selectedTerm,
    stats,
    terms,
    termIds,
    filter,
    permission,
    termsLoading,
    termCount,
    saving,
    deleting,
    notification,
    project
  } = state.glossary
  const query = location.query
  return {
    terms,
    termIds,
    termCount,
    termsLoading,
    transLocales: stats.transLocales,
    srcLocale: stats.srcLocale,
    filterText: filter,
    selectedTerm: selectedTerm,
    selectedTransLocale: query.locale,
    permission,
    location: state.routing.location,
    saving,
    deleting,
    notification,
    page: query.page,
    pageSize: query.size,
    project
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleInitLoad: (projectSlug) => {
      dispatch(glossaryInitialLoad(projectSlug))
    },
    handleSelectTerm: (termId) => dispatch(glossarySelectTerm(termId)),
    handleTermFieldUpdate: (field, event) => {
      dispatch(glossaryUpdateField({ field, value: event.target.value || '' }))
    },
    handleDeleteTerm: (termId) => dispatch(glossaryDeleteTerm(termId)),
    handleResetTerm: (termId) => dispatch(glossaryResetTerm(termId)),
    handleUpdateTerm: (term, needRefresh) =>
      dispatch(glossaryUpdateTerm(term, needRefresh)),
    handlePageSizeChange: (size) =>
      dispatch(glossaryUpdatePageSize(size.value)),
    gotoFirstPage: (currentPage, totalPage) =>
      dispatch(glossaryGoFirstPage(currentPage, totalPage)),
    gotoPreviousPage: (currentPage, totalPage) =>
      dispatch(glossaryGoPreviousPage(currentPage, totalPage)),
    gotoNextPage: (currentPage, totalPage) =>
      dispatch(glossaryGoNextPage(currentPage, totalPage)),
    gotoLastPage: (currentPage, totalPage) =>
      dispatch(glossaryGoLastPage(currentPage, totalPage))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Glossary)
