import cx from 'classnames'
import React, { PropTypes } from 'react'
import SuggestionsHeader from './SuggestionsHeader'
import SuggestionsBody from './SuggestionsBody'
import SuggestionDetailsModal from './SuggestionDetailsModal'
import { isUndefined, pick } from 'lodash'
import { connect } from 'react-redux'
import {
  copySuggestionN,
  clearSearch,
  changeSearchText,
  diffSettingChanged,
  showDetailForSuggestionByIndex,
  toggleSearchType,
  toggleSuggestions
} from '../actions/suggestions'

const DO_NOT_RENDER = null
const SEARCH_TYPE_PHRASE = 'phrase'
const SEARCH_TYPE_TEXT = 'text'

/**
 * Panel to search for and display suggestions.
 */
const SuggestionsPanel = React.createClass({
  propTypes: {
    searchToggle: PropTypes.func.isRequired,
    clearSearch: PropTypes.func.isRequired,
    changeSearchText: PropTypes.func.isRequired,
    showDetail: PropTypes.func.isRequired,
    showDetailModalForIndex: PropTypes.number,
    // likely want to move this switching to a higher level
    showPanel: PropTypes.bool.isRequired,
    searchType: PropTypes.oneOf(
      [SEARCH_TYPE_PHRASE, SEARCH_TYPE_TEXT]).isRequired,
    search: PropTypes.shape({
      suggestions: PropTypes.array.isRequired
    }).isRequired
  },

  hideDetail () {
    this.props.showDetail(undefined)
  },

  render: function () {
    if (!this.props.showPanel) {
      return DO_NOT_RENDER
    }

    const className = cx('Editor-suggestions Editor-panel u-bgHigh', {
      'is-search-active': this.props.searchType === SEARCH_TYPE_TEXT
    })

    const headerProps = pick(this.props, ['showDiff', 'onDiffChange',
      'closeSuggestions', 'search', 'phraseSelected', 'searchType'])

    // TODO use imported actions for these instead of passing in props
    headerProps.search.toggle = this.props.searchToggle
    headerProps.search.clear = this.props.clearSearch
    headerProps.search.changeText = this.props.changeSearchText

    const bodyProps = pick(this.props, ['copySuggestion', 'showDiff',
      'showDetail', 'phraseSelected', 'search', 'searchType'])

    const { showDetailModalForIndex, search } = this.props
    var detailModal
    if (isUndefined(showDetailModalForIndex)) {
      detailModal = undefined
    } else {
      detailModal = (
        <SuggestionDetailsModal
          onClose={this.hideDetail}
          suggestion={search.suggestions[showDetailModalForIndex]} />
      )
    }

    return (
      <aside
        id="editor-suggestions"
        className={className}>
        <SuggestionsHeader {...headerProps} />
        <SuggestionsBody {...bodyProps} />
        {detailModal}
      </aside>
    )
  }
})

function mapStateToProps (state) {
  const { search, searchType, searchByPhrase, textSearch } = state.suggestions
  const selectedPhraseId = state.phrases.selectedPhraseId
  const phraseSelected = !!selectedPhraseId
  var specificSearch = search

  // FIXME seeing search = undefined here even though it is in default state

  if (searchType === SEARCH_TYPE_PHRASE) {
    if (phraseSelected) {
      const phraseSearch = searchByPhrase[selectedPhraseId]
      if (phraseSearch) {
        specificSearch = {
          ...search,
          ...phraseSearch
        }
      } else {
        // FIXME make it so I don't need to specify suggestions when loading
        specificSearch = {
          ...search,
          loading: true,
          searchStrings: [],
          suggestions: []
        }
      }
    } else {
      // show no phrase search if no TU (phrase) is selected
      specificSearch = {
        ...search,
        loading: false,
        searchStrings: [],
        suggestions: []
      }
    }
  } else if (searchType === SEARCH_TYPE_TEXT) {
    specificSearch = {
      ...search,
      ...textSearch
    }
  } else {
    console.error('invalid state.suggestions.searchType', searchType)
  }

  return {
    ...state.suggestions,
    search: specificSearch,
    showPanel: state.ui.panels.suggestions.visible,
    phraseSelected: state.phrases.selectedPhraseId !== undefined
  }
}

function mapDispatchToProps (dispatch) {
  return {
    onDiffChange: () => dispatch(diffSettingChanged()),
    closeSuggestions: () => dispatch(toggleSuggestions()),
    searchToggle: () => dispatch(toggleSearchType()),
    clearSearch: () => dispatch(clearSearch()),
    changeSearchText: event => {
      dispatch(changeSearchText(event.target.value))
    },
    copySuggestion: (index) => {
      dispatch(copySuggestionN(index))
    },
    showDetail: (index) => {
      dispatch(showDetailForSuggestionByIndex(index))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SuggestionsPanel)
