import React, { PropTypes } from 'react'
import NoSuggestionsPanel from '../components/NoSuggestionsPanel'
import SuggestionList from '../components/SuggestionList'
import LoaderText from '../../components/LoaderText'
/**
 * Display all suggestions that match the current search.
 */
class SuggestionsBody extends React.Component {
  static propTypes = {
    copySuggestion: PropTypes.func.isRequired,
    searchType: PropTypes.oneOf(['phrase', 'text']).isRequired,
    search: PropTypes.shape({
      searchStrings: PropTypes.arrayOf(PropTypes.string),
      loading: PropTypes.bool.isRequired,
      input: PropTypes.shape({
        focused: PropTypes.bool.isRequired
      }).isRequired,
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
    }).isRequired,
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired,
    phraseSelected: PropTypes.bool.isRequired
  }

  renderContent = () => {
    const isTextSearch = this.props.searchType === 'text'
    const phraseSelected = this.props.phraseSelected
    const hasSearch = this.props.search.searchStrings.length !== 0
    const hasSuggestions = this.props.search.suggestions.length !== 0

    if (this.props.search.loading) {
      return (
        <div className="u-posCenterCenter u-textEmpty u-textCenter">
          <div className="u-sMB-1-4">
            <div className="search-sugg">
              <LoaderText loading loadingText='Loading suggestions' />
            </div>
          </div>
        </div>
      )
    }

    if (!hasSearch && !phraseSelected) {
      return (
        <NoSuggestionsPanel
          icon="suggestions"
          message="Select a phrase or enter a search term" />
      )
    }

    if (isTextSearch && !hasSearch) {
      return (
        <NoSuggestionsPanel
          icon="search"
          message="Enter a search term" />
      )
    }

    if (hasSearch && !hasSuggestions) {
      const noMatchingSuggestionMessage = isTextSearch
        ? 'No matching suggestions for the current search'
        : 'No matching suggestions for the currently selected phrase'
      return (
        <NoSuggestionsPanel
          icon="suggestions"
          message={noMatchingSuggestionMessage} />
      )
    }

    return (
      <SuggestionList
        search={this.props.search.searchStrings}
        showDiff={this.props.showDiff}
        showDetail={this.props.showDetail}
        suggestions={this.props.search.suggestions}
        copySuggestion={this.props.copySuggestion} />
    )
  }

  render () {
    return (
      <div className="Editor-suggestionsBody u-bgHigh">
        {this.renderContent()}
      </div>
    )
  }
}

export default SuggestionsBody
