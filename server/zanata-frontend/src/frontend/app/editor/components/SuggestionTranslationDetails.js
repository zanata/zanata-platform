import Button from './Button'
import React from 'react'
import PropTypes from 'prop-types'
import SuggestionMatchPercent from './SuggestionMatchPercent'
import SuggestionUpdateMessage from './SuggestionUpdateMessage'

/**
 * Display metadata and copy button for the translations of a suggestion.
 */
class SuggestionTranslationDetails extends React.Component {
  static propTypes = {
    copySuggestion: PropTypes.func.isRequired,
    suggestion: PropTypes.shape({
      copying: PropTypes.bool.isRequired,
      matchType: PropTypes.string.isRequired,
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.string.isRequired,
        contentState: PropTypes.string
      })),
      similarityPercent: PropTypes.number
    })
  }

  user = (suggestion) => {
    const topMatch = suggestion.matchDetails[0]
    return topMatch.lastModifiedBy || 'Anonymous'
  }

  lastChanged = (suggestion) => {
    const topMatch = suggestion.matchDetails[0]
    if (topMatch.type === 'IMPORTED_TM') {
      return topMatch.lastChanged
    }
    if (topMatch.type === 'LOCAL_PROJECT') {
      return topMatch.lastModifiedDate
    }
    console.error('match type not recognized for looking up date: ' +
                  topMatch.type)
  }

  render () {
    const { copySuggestion, suggestion } = this.props
    const { copying, matchType, similarityPercent } = suggestion
    const label = copying ? 'Copied' : 'Copy Translation'
    const user = this.user(suggestion)
    // TODO convert these to Date in the api module instead
    //      (so new date instances are not created every render)
    const lastChanged = new Date(this.lastChanged(suggestion))

    return (
      <div className="TransUnit-details">
        <div className="u-floatLeft u-sizeLineHeight-1">
          <SuggestionUpdateMessage
            matchType={matchType}
            user={user}
            lastChanged={lastChanged} />
        </div>
        <div className="u-floatRight u-sm-floatNone">
          <ul className="u-listInline u-sizeLineHeight-1">
            <li className="u-floatRight">
              <SuggestionMatchPercent
                matchType={matchType}
                percent={similarityPercent} />
            </li>
            <li>
              <Button
                className="EditorButton Button--small u-rounded Button--primary
                           u-sizeWidth-4"
                disabled={copying}
                onClick={copySuggestion}
                title={label}>
                {label}
              </Button>
            </li>
          </ul>
        </div>
      </div>
    )
  }
}

export default SuggestionTranslationDetails
