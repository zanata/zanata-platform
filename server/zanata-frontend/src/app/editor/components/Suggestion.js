import React from 'react'
import * as PropTypes from 'prop-types'
import SuggestionSources from './SuggestionSources'
import SuggestionTranslations from './SuggestionTranslations'

const matchTypeClass = {
  imported: 'TransUnit--secondary',
  translated: 'TransUnit--success',
  approved: 'TransUnit--highlight'
}

/**
 * Display a single suggestion source, translation and metadata.
 */
class Suggestion extends React.Component {
  static propTypes = {
    copySuggestion: PropTypes.func.isRequired,
    index: PropTypes.number.isRequired,
    suggestion: PropTypes.shape({
      // true when the translation has just been copied
      copying: PropTypes.bool.isRequired,
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.oneOf(
          ['IMPORTED_TM', 'LOCAL_PROJECT']).isRequired,
        contentState: PropTypes.oneOf(['Translated', 'Approved'])
      })),
      similarityPercent: PropTypes.number,
      sourceContents: PropTypes.arrayOf(
        PropTypes.string).isRequired,
      targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
    }),
    search: PropTypes.arrayOf(PropTypes.string),
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired,
    isRTLSource: PropTypes.bool.isRequired,
    isRTLTarget: PropTypes.bool.isRequired
  }

  /**
   * Calculate the match type for the suggestion
   */
  matchType = (suggestion) => {
    let topMatch = suggestion.matchDetails[0]

    if (topMatch.type === 'IMPORTED_TM') {
      return 'imported'
    }
    if (topMatch.type === 'LOCAL_PROJECT') {
      if (topMatch.contentState === 'Translated') {
        return 'translated'
      }
      if (topMatch.contentState === 'Approved') {
        return 'approved'
      }
    }
    console.error('Unable to generate row display type for top match')
  }

  copySuggestion = () => {
    this.props.copySuggestion(this.props.index)
  }

  showDetail = () => {
    this.props.showDetail(this.props.index)
  }

  render () {
    const matchType = this.matchType(this.props.suggestion)
    const className = 'TransUnit TransUnit--suggestion ' +
                        matchTypeClass[matchType]
    const suggestion = {
      ...this.props.suggestion,
      matchType
    }
    const sourceDirectionClass = this.props.isRTLSource ? 'rtl' : 'ltr'
    const targetDirectionClass = this.props.isRTLTarget ? 'rtl' : 'ltr'

    const props = {
      ...this.props,
      suggestion,
      directionClass: sourceDirectionClass,
      showDetail: this.showDetail
    }
    return (
      <div
        className={className}>
        <div className="TransUnit-status" />
        <SuggestionSources {...props} />
        <SuggestionTranslations
          directionClass={targetDirectionClass}
          copySuggestion={this.copySuggestion}
          suggestion={suggestion} />
      </div>
    )
  }
}

export default Suggestion
