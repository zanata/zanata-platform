import React from 'react'
import PropTypes from 'prop-types'
import SuggestionContents from './SuggestionContents'
import SuggestionTranslationDetails from './SuggestionTranslationDetails'
import { createAction } from 'redux-actions'
import { LOCALE_SELECTED } from '../actions/header-action-types'

export const localeDetails = createAction(LOCALE_SELECTED)

/**
 * Show all translations for a suggestion, with translation metadata.
 */
class SuggestionTranslations extends React.Component {
  static propTypes = {
    copySuggestion: PropTypes.func.isRequired,
    suggestion: PropTypes.shape({
      copying: PropTypes.bool.isRequired,
      matchType: PropTypes.string.isRequired,
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.string.isRequired,
        contentState: PropTypes.string
      })),
      similarityPercent: PropTypes.number,
      sourceContents: PropTypes.arrayOf(
        PropTypes.string).isRequired,
      targetContents: PropTypes.arrayOf(PropTypes.string).isRequired,
      directionClass: PropTypes.object.isRequired,
      isLtr: PropTypes.bool.isRequired
    })
  }

  render () {
    const directionClass = localeDetails.isLtr ? 'ltr' : 'rtl'
    return (
      <div className="TransUnit-panel TransUnit-translation u-sPV-1-2">
        <span className={directionClass}>
          <SuggestionContents
            plural={this.props.suggestion.sourceContents.length > 1}
            contents={this.props.suggestion.targetContents} />
        </span>
        <SuggestionTranslationDetails {... this.props} />
      </div>
    )
  }
}

export default SuggestionTranslations
