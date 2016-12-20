/**
 * Displays just the content text for source and translation of a suggestion
 */

import React, { Component, PropTypes } from 'react'
import SuggestionContents from '../../components/SuggestionContents'
import cx from 'classnames'

class PlainSuggestionContents extends Component {

  matchTypeClass (matchType) {
    return ({
      imported: 'TransUnit--secondary',
      translated: 'TransUnit--success',
      approved: 'TransUnit--highlight'
    })[matchType]
  }

  render () {
    const { sourceContents, targetContents } = this.props.suggestion
    const className = cx('TransUnit TransUnit--suggestion u-bgHigh u-sMB-1',
      this.matchTypeClass(this.props.matchType))
    return (
      <div className={className}>
        <div className="TransUnit-status" />
        <div className="TransUnit-panel TransUnit-source">
          <SuggestionContents
            plural={sourceContents.length > 1}
            contents={sourceContents} />
        </div>
        <div className="TransUnit-panel TransUnit-translation u-sPV-1-2">
          <SuggestionContents
            plural={sourceContents.length > 1}
            contents={targetContents} />
        </div>
      </div>
    )
  }
}

PlainSuggestionContents.propTypes = {
  /* Optional match type colour to display on the status bar. */
  matchType: PropTypes.string,
  suggestion: PropTypes.shape({
    sourceContents: PropTypes.arrayOf(PropTypes.string).isRequired,
    targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
  }).isRequired
}

export default PlainSuggestionContents
