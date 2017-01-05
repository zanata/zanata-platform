import React, { PropTypes } from 'react'
import Suggestion from './Suggestion'
import { pick } from 'lodash'

/**
 * Display all suggestions that match the current search.
 */
const SuggestionList = React.createClass({
  propTypes: {
    copySuggestion: PropTypes.func.isRequired,
    search: PropTypes.arrayOf(PropTypes.string),
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired,

    suggestions: PropTypes.arrayOf(PropTypes.shape({
      // true when the translation has just been copied
      copying: PropTypes.bool.isRequired,
      suggestion: PropTypes.shape({
        matchDetails: PropTypes.arrayOf(PropTypes.shape({
          type: PropTypes.string.isRequired,
          contentState: PropTypes.string
        })),
        similarityPercent: PropTypes.number,
        sourceContents: PropTypes.arrayOf(
          PropTypes.string).isRequired,
        targetContents: PropTypes.arrayOf(
          PropTypes.string).isRequired
      })
    }))
  },

  render: function () {
    const sharedProps = pick(this.props,
      ['copySuggestion', 'search', 'showDiff', 'showDetail'])

    const suggestions = this.props.suggestions.map((suggestion, index) => {
      return <Suggestion key={index}
        index={index}
        suggestion={suggestion}
        {...sharedProps} />
    })

    return (
      <div>
        {suggestions}
      </div>
    )
  }
})

export default SuggestionList
