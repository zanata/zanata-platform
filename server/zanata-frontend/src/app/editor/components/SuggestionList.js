import React from 'react'
import * as PropTypes from 'prop-types'
import Suggestion from './Suggestion'
import { pick } from 'lodash'

/**
 * Display all suggestions that match the current search.
 */
class SuggestionList extends React.Component {
  static propTypes = {
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
    })),
    isRTLSource: PropTypes.bool.isRequired,
    isRTLTarget: PropTypes.bool.isRequired
  }

  render () {
    const sharedProps = pick(this.props,
        ['copySuggestion', 'search', 'showDiff', 'showDetail', 'isRTLSource',
          'isRTLTarget'])

    // @ts-ignore any
    const suggestions = this.props.suggestions.map((suggestion, index) => {
      return <Suggestion key={index}
        index={index}
        suggestion={suggestion}
        {...sharedProps} />
    })

    return (
      <>
        {suggestions}
      </>
    )
  }
}

export default SuggestionList
