/**
 * Displays just the content text for source and translation of a suggestion
 */

import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import SuggestionContents from '../../components/SuggestionContents'
import cx from 'classnames'

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
    directionClassTarget: PropTypes.object.isRequired
  }

  matchTypeClass = (matchType) => {
    return ({
      imported: 'TransUnit--secondary',
      translated: 'TransUnit--success',
      approved: 'TransUnit--highlight'
    })[matchType]
  }

  render () {
    const {suggestion, directionClassSource, directionClassTarget} = this.props
    const {sourceContents, targetContents} = suggestion
    const displayHeader = this.props.displayHeader
    const className = cx('TransUnit TransUnit--suggestion u-bgHigh u-sMB-1',
      this.matchTypeClass(this.props.matchType))

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
