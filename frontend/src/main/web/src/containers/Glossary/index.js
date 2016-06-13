import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { isUndefined, size } from 'lodash'
import ReactList from 'react-list'
import {
  LoaderText,
  Page,
  ScrollView,
  View,
  Notification,
  Row,
  ButtonLink,
  Icon
} from '../../components'
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
  GLOSSARY_PAGE_SIZE
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
    let entry = undefined
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
      page
    } = this.props

    const totalPage = Math.floor(termCount / GLOSSARY_PAGE_SIZE) +
      (termCount % GLOSSARY_PAGE_SIZE > 0 ? 1 : 0)

    const currentPage = page ? parseInt(page) : 1
    const displayPaging = totalPage > 1
    const listPadding = displayPaging ? 'Pb(r2)' : 'Pb(r2) Pt(r6)'

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
          {displayPaging &&
            <View theme={{ base: {p: 'Pt(r6)--sm Pt(r4)', fld: 'Fld(rr)'} }}>
              <Row>
                <ButtonLink disabled={currentPage <= 1}
                  onClick={() => { gotoFirstPage(currentPage, totalPage) }}>
                  <Icon name='previous' size='1' />
                </ButtonLink>
                <ButtonLink disabled={currentPage <= 1}
                  onClick={() => { gotoPreviousPage(currentPage, totalPage) }}>
                  <Icon name='chevron-left' size='1' />
                </ButtonLink>
                <span className='C(muted) Mx(re)'>
                  {currentPage} of {totalPage}
                </span>
                <ButtonLink disabled={currentPage === totalPage}
                  onClick={() => { gotoNextPage(currentPage, totalPage) }}>
                  <Icon name='chevron-right' size='1' />
                </ButtonLink>
                <ButtonLink disabled={currentPage === totalPage}
                  onClick={() => { gotoLastPage(currentPage, totalPage) }}>
                  <Icon name='next' size='1' />
                </ButtonLink>
                <span className='Mx(rq) C(muted)'>
                  <Row>
                    <Icon name='glossary' size='1' /> {termCount}
                  </Row>
                </span>
              </Row>
            </View>
          }
          <View theme={{ base: {p: listPadding} }}>
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
                  ref={(c) => { this.list = c }}
                />)
            }
          </View>
        </ScrollView>
      </Page>
    )
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
  page: PropTypes.number
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
    page: parseInt(state.routing.location.query.page)
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleSelectTerm: (termId) => dispatch(glossarySelectTerm(termId)),
    handleTermFieldUpdate: (field, event) => {
      dispatch(glossaryUpdateField({ field, value: event.target.value || '' }))
    },
    handleDeleteTerm: (termId) => dispatch(glossaryDeleteTerm(termId)),
    handleResetTerm: (termId) => dispatch(glossaryResetTerm(termId)),
    handleUpdateTerm: (term, needRefresh) =>
      dispatch(glossaryUpdateTerm(term, needRefresh)),
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
