import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { isUndefined, size, map } from 'lodash'
import ReactList from 'react-list'
import {
  LoaderText,
  Page,
  ScrollView,
  View,
  Row,
  ButtonLink,
  Icon,
  Select
} from 'zanata-ui'
import { Notification } from '../../components'
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
} from '../../actions/glossary'
import ViewHeader from './ViewHeader'
import Entry from './Entry'

const loadingContainerTheme = {
  base: {
    ai: 'Ai(c)',
    flxg: 'Flxg(1)',
    jc: 'Jc(c)',
    w: 'W(100%)'
  }
}
/**
 * Root component for Glossary page
 */
class Glossary extends Component {
  componentDidMount () {
    this.props.handleInitLoad()
  }

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
      handlePageSizeChange
    } = this.props

    const intPageSize = pageSize ? parseInt(pageSize) : PAGE_SIZE_DEFAULT
    const totalPage = Math.floor(termCount / intPageSize) +
      (termCount % intPageSize > 0 ? 1 : 0)
    const currentPage = page ? parseInt(page) : 1
    const displayPaging = totalPage > 1
    const pageSizeOption = map(PAGE_SIZE_SELECTION, (size) => {
      return {label: size, value: size}
    })
    /* eslint-disable react/jsx-no-bind */
    return (
      <Page>
        {notification &&
          (<Notification severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
          )
        }
        <Helmet title='Glossary' />
        <ScrollView>
          <ViewHeader />
          <View theme={{ base: {p: 'Pt(r6)--sm Pt(r4)', fld: 'Fld(rr)'} }}>
            <Row>
              {termCount > 0 &&
                <Row>
                  <span className='Hidden--lesm Pend(rq)'>Show</span>
                  <Select options={pageSizeOption}
                    placeholder='Terms per page'
                    value={intPageSize}
                    name='glossary-page'
                    className='Mend(re) W(ms8)'
                    searchable={false}
                    clearable={false}
                    onChange={handlePageSizeChange} />
                </Row>
              }
              {displayPaging &&
                <div className='D(f)'>
                  <ButtonLink disabled={currentPage <= 1}
                    title='First page'
                    onClick={() => { gotoFirstPage(currentPage, totalPage) }}>
                    <Icon name='previous' size='1' />
                  </ButtonLink>
                  <ButtonLink disabled={currentPage <= 1}
                    title='Previous page'
                    onClick={
                    () => { gotoPreviousPage(currentPage, totalPage) }}>
                    <Icon name='chevron-left' size='1' />
                  </ButtonLink>
                  <span className='C(muted) Mx(re)'>
                    {currentPage} of {totalPage}
                  </span>
                  <ButtonLink disabled={currentPage === totalPage}
                    title='Next page'
                    onClick={() => { gotoNextPage(currentPage, totalPage) }}>
                    <Icon name='chevron-right' size='1' />
                  </ButtonLink>
                  <ButtonLink disabled={currentPage === totalPage}
                    title='Last page'
                    onClick={() => { gotoLastPage(currentPage, totalPage) }}>
                    <Icon name='next' size='1' />
                  </ButtonLink>
                  <span className='Mx(rq) C(muted)'
                    title='Total glossary terms'>
                    <Row>
                      <Icon name='glossary' size='1' /> {termCount}
                    </Row>
                  </span>
                </div>
                }
            </Row>
          </View>

          <View theme={{ base: {p: 'Pb(r2)'} }}>
            {termsLoading && !termCount
              ? (<View theme={loadingContainerTheme}>
                <LoaderText theme={{ base: { fz: 'Fz(ms1)' } }}
                  size='1' loading />
              </View>)
              : (<ReactList
                useTranslate3d
                itemRenderer={::this.renderItem}
                length={size(terms)}
                type='uniform'
                ref={(c) => { this.list = c }} />)
            }
          </View>
        </ScrollView>
      </Page>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

Glossary.propTypes = {
  /**
   * Object of glossary id with term
   */
  terms: PropTypes.object,
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

const mapStateToProps = (state) => {
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
    notification
  } = state.glossary
  const query = state.routing.location.query
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
    pageSize: query.size
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleInitLoad: () => {
      dispatch(glossaryInitialLoad())
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
