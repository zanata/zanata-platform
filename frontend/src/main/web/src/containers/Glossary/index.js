import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { debounce, isUndefined } from 'lodash'
import { replaceRouteQuery } from '../../utils/RoutingHelpers'
import ReactList from 'react-list'
import {
  LoaderText,
  Page,
  ScrollView,
  View,
  Notification
} from '../../components'
import {
  glossaryDeleteTerm,
  glossaryGetTermsIfNeeded,
  glossaryResetTerm,
  glossarySelectTerm,
  glossaryUpdateField,
  glossaryUpdateIndex,
  glossaryUpdateTerm
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
  constructor () {
    super()
    // Need to add the debounce to onScroll here
    // So it creates a new debounce for each instance
    this.onScroll = debounce(this.onScroll, 100)
  }

  isTermLocaleSelected (term, selectedLocale) {
    return term.transTerm && (term.transTerm.locale === selectedLocale)
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

  onScroll () {
    // Debounced by 100ms in super()
    if (!this.list) return
    const {
      location,
      handleUpdateIndex,
      handleGetTermsIfNeeded
    } = this.props
    const loadingThreshold = 250
    const indexRange = this.list.getVisibleRange()
    const newIndex = indexRange[0]
    const newIndexEnd = indexRange[1]
    replaceRouteQuery(location, {
      index: newIndex
    })
    handleUpdateIndex(newIndex)
    handleGetTermsIfNeeded(newIndex)
    // If close enough, load the prev/next page too
    const preIndex = newIndex - loadingThreshold
    const nextIndex = newIndexEnd + loadingThreshold
    preIndex > 0 && handleGetTermsIfNeeded(preIndex)
    handleGetTermsIfNeeded(nextIndex)
  }

  render () {
    const {
      termsLoading,
      termCount,
      scrollIndex,
      notification
    } = this.props
    return (
      <Page>
        {notification
          ? (<Notification severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
          )
          : undefined
        }
        <Helmet title='Glossary' />
        <ScrollView onScroll={::this.onScroll}>
          <ViewHeader />
          <View theme={{ base: {p: 'Pt(r6) Pb(r2)'} }}>
            {termsLoading && !termCount
              ? (<View theme={loadingContainerTheme}>
                  <LoaderText theme={{ base: { fz: 'Fz(ms1)' } }}
                    size='1' loading />
                  </View>)
              : (<ReactList
                  useTranslate3d
                  itemRenderer={::this.renderItem}
                  length={termCount}
                  type='uniform'
                  initialIndex={scrollIndex || -5}
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
  scrollIndex: PropTypes.number,
  permission: PropTypes.object,
  location: PropTypes.object,
  saving: PropTypes.object,
  deleting: PropTypes.object,
  notification: PropTypes.object
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
    scrollIndex: Number.parseInt(query.index, 10),
    permission,
    location: state.routing.location,
    saving,
    deleting,
    notification
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
    handleUpdateIndex: (newIndex) => dispatch(glossaryUpdateIndex(newIndex)),
    handleGetTermsIfNeeded: (newIndex) =>
      dispatch(glossaryGetTermsIfNeeded(newIndex))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Glossary)
