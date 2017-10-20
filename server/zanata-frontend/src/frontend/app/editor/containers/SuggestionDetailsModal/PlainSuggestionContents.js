/**
 * Displays just the content text for source and translation of a suggestion
 */

import React, { Component } from 'react'
import PropTypes from 'prop-types'
import SuggestionContents from '../../components/SuggestionContents'
import cx from 'classnames'
import { createAction } from 'redux-actions'
import { LOCALE_SELECTED } from '../../actions/header-action-types'

export const localeDetails = createAction(LOCALE_SELECTED)

class PlainSuggestionContents extends Component {
  static propTypes = {
    /* Optional match type colour to display on the status bar. */
    matchType: PropTypes.string,
    suggestion: PropTypes.shape({
      sourceContents: PropTypes.arrayOf(PropTypes.string).isRequired,
      targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
    }).isRequired,
    displayHeader: PropTypes.bool,
    directionClassSource: PropTypes.object.isRequired,
    directionClassTarget: PropTypes.object.isRequired,
    isLtrSource: PropTypes.bool.isRequired,
    isLtrTarget: PropTypes.bool.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      // TODO location detection so defaults isLtr* = true/false can be
      // removed
      isLtrSource: true,
      isLtrTarget: false
    }
  }

  matchTypeClass = (matchType) => {
    return ({
      imported: 'TransUnit--secondary',
      translated: 'TransUnit--success',
      approved: 'TransUnit--highlight'
    })[matchType]
  }

  render () {
    const { sourceContents, targetContents } = this.props.suggestion
    const displayHeader = this.props.displayHeader
    const className = cx('TransUnit TransUnit--suggestion u-bgHigh u-sMB-1',
      this.matchTypeClass(this.props.matchType))
    const directionClassSource = localeDetails.isLtrSource ? 'ltr' : 'rtl'
    const directionClassTarget = localeDetails.isLtrTarget ? 'ltr' : 'rtl'

    return (
      <div className={className}>
        <div className="TransUnit-status" />
        {displayHeader && <span className="TransUnit-sourceHeading">
        Source</span>}
        <div className={directionClassSource + ' TransUnit-panel' +
        ' TransUnit-source'}>
          <SuggestionContents
            plural={sourceContents.length > 1}
            contents={sourceContents} />
        </div>
        {displayHeader && <span className="TransUnit-targetHeading">
        Translation</span>}
        <div className={directionClassTarget + ' TransUnit-panel' +
        ' TransUnit-translation u-sPV-1-2'}>
          <SuggestionContents
            plural={sourceContents.length > 1}
            contents={targetContents} />
        </div>
      </div>
    )
  }
}

export default PlainSuggestionContents
