/**
 * Displays just the content text for source and translation of a suggestion
 */

import React, { Component, PropTypes } from 'react'
import SuggestionContents from '../../components/SuggestionContents'

class PlainSuggestionContents extends Component {
  render () {
    const { sourceContents, targetContents } = this.props.suggestion
    return (
      <div className="TransUnit TransUnit--suggestion u-bgHigh u-sMB-1">
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
  suggestion: PropTypes.shape({
    sourceContents: PropTypes.arrayOf(PropTypes.string).isRequired,
    targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
  }).isRequired
}

export default PlainSuggestionContents
