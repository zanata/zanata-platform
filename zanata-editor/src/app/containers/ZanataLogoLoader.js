import { dashboardUrl } from '../api'
import cx from 'classnames'
import { some } from 'lodash'
import { LogoLoader } from 'zanata-ui'
import React, { PropTypes } from 'react'
import { connect } from 'react-redux'

/**
 * Logo that links to the dashboard and animates when anything is loading.
 */
const ZanataLogoLoader = ({ inverted, loading }) => {
  // FIXME scrollbar width adjustment
  const className = cx('Editor-loader', {
    'is-minimised': inverted
  })
  return (
    <a className={className}
      href={dashboardUrl}>
      <LogoLoader {...{ inverted, loading }} />
    </a>
  )
}

ZanataLogoLoader.propTypes = {
  inverted: PropTypes.bool.isRequired,
  loading: PropTypes.bool.isRequired
}

function mapStateToProps ({phrases, suggestions, ui}) {
  const { fetchingDetail, fetchingList } = phrases
  const fetchingPhrases = fetchingList || fetchingDetail
  const saveInProgress = some(phrases.detail, (phrase) => {
    return !!phrase.inProgressSave
  })
  const suggestionSearchActive = suggestions.textSearch.loading ||
    some(suggestions.searchByPhrase, (search) => {
      return search.loading
    })
  // TODO maybe set loading when fetching stats too
  return {
    inverted: !ui.panels.navHeader.visible,
    loading: fetchingPhrases || saveInProgress || suggestionSearchActive
  }
}

export default connect(mapStateToProps)(ZanataLogoLoader)
