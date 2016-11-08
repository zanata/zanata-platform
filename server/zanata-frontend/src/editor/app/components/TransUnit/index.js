import React, { PropTypes } from 'react'
import cx from 'classnames'
import TransUnitStatus from '../TransUnitStatus'
import TransUnitSourcePanel from '../TransUnitSourcePanel'
import TransUnitTranslationPanel from '../TransUnitTranslationPanel'
import { connect } from 'react-redux'
import { pick } from 'lodash'
import { toggleDropdown, closeDropdown } from '../../actions'
import {
  cancelEdit,
  copyFromSource,
  savePhraseWithStatus,
  selectPhrase,
  selectPhrasePluralIndex,
  translationTextInputChanged,
  undoEdit
} from '../../actions/phrases'
import { togglePhraseSuggestions } from '../../actions/suggestions'

/**
 * Single row in the editor displaying a whole phrase.
 * Including source, translation, metadata and editing
 * facilities.
 */
const TransUnit = React.createClass({
  propTypes: {
    phrase: PropTypes.object.isRequired,
    saveAsMode: PropTypes.bool.isRequired,
    selectPhrase: PropTypes.func.isRequired,
    isFirstPhrase: PropTypes.bool.isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired,
    // savingStatusId: PropTypes.oneOf([
    //   'untranslated',
    //   'needswork',
    //   'translated',
    //   'approved'
    // ])
    selected: PropTypes.bool.isRequired
  },

  getInitialState: () => {
    return {
      saveDropdownKey: {}
    }
  },

  transUnitClassByStatus: {
    untranslated: 'TransUnit--neutral',
    needswork: 'TransUnit--unsure',
    translated: 'TransUnit--success',
    approved: 'TransUnit--highlight',
    rejected: 'TransUnit--warning'
  },

  selectPhrase () {
    const { phrase, selectPhrase } = this.props
    selectPhrase(phrase.id)
  },

  render: function () {
    // TODO different display if isLoading (need to add or infer isLoading)

    const displayStatus = this.props.phrase.inProgressSave
      ? this.props.phrase.inProgressSave.status
      : this.props.phrase.status

    const className = cx('TransUnit',
      this.transUnitClassByStatus[displayStatus],
      {
        'is-focused': this.props.selected,
        'is-first': this.props.isFirstPhrase
      })

    const phraseSourcePanelProps = pick(this.props, [
      'cancelEdit',
      'copyFromSource',
      'phrase',
      'selected',
      'sourceLocale'
    ])

    const phraseTranslationPanelProps = pick(this.props, [
      'cancelEdit',
      'openDropdown',
      'phrase',
      'saveAsMode',
      'savePhraseWithStatus',
      'selected',
      'selectPhrasePluralIndex',
      'showSuggestions',
      'suggestionCount',
      'suggestionSearchType',
      'textChanged',
      'toggleDropdown',
      'toggleSuggestionPanel',
      'translationLocale',
      'undoEdit'
    ])

    return (
      <div className={className}
        onClick={this.selectPhrase}>
        <TransUnitStatus phrase={this.props.phrase} />
        <TransUnitSourcePanel {...phraseSourcePanelProps} />
        <TransUnitTranslationPanel {...phraseTranslationPanelProps}
          saveDropdownKey={this.props.phrase.id} />
      </div>
    )
  }
})

function mapStateToProps (state, ownProps) {
  const index = ownProps.index
  const phrase = ownProps.phrase
  const sourceLocale = state.context.sourceLocale

  // FIXME do not want to show 0 while it is loading, maybe show nothing or show
  //       that the suggestion search is in progress if phraseSearch.loading
  const phraseSearch = state.suggestions.searchByPhrase[phrase.id]
  const suggestionCount = phraseSearch ? phraseSearch.suggestions.length : 0
  const suggestionSearchType = state.suggestions.searchType
  const showSuggestions = state.ui.panels.suggestions.visible

  const passThroughProps = pick(state, [
    'openDropdown'
  ])

  const saveAsMode = state.phrases.saveAsMode

  return {
    ...passThroughProps,
    // TODO add something for looking up locale name instead, or check
    //      whether it comes with the response.
    translationLocale: {
      id: state.context.lang,
      name: getLocaleName(state)
    },
    phrase,
    openDropdown: state.dropdown.openDropdownKey,
    sourceLocale: {
      id: sourceLocale.localeId,
      name: sourceLocale.name
    },
    saveAsMode,
    isFirstPhrase: index === 0,
    selected: state.phrases.selectedPhraseId === phrase.id,
    // savingStatusId: phrase.isSaving ? phrase.savingStatusId : undefined,
    suggestionCount,
    suggestionSearchType,
    showSuggestions
  }
}

function getLocaleName (state) {
  const l = state.headerData.context.projectVersion.locales[state.context.lang]
  return l ? l.name : state.context.lang
}

function mapDispatchToProps (dispatch, ownProps) {
  return {
    cancelEdit: (event) => {
      event.stopPropagation()
      dispatch(cancelEdit())
      dispatch(closeDropdown())
    },
    copyFromSource: (sourceIndex) => {
      dispatch(copyFromSource(ownProps.phrase.id, sourceIndex))
    },
    savePhraseWithStatus: (phrase, status) => {
      dispatch(savePhraseWithStatus(phrase, status))
    },
    selectPhrase: () => {
      dispatch(selectPhrase(ownProps.phrase.id))
    },
    selectPhrasePluralIndex: (phraseId, index) => {
      dispatch(selectPhrasePluralIndex(phraseId, index))
    },
    textChanged: (id, index, event) => {
      const text = event.target.value
      dispatch(translationTextInputChanged(id, index, text))
    },
    toggleDropdown: (key) => {
      dispatch(toggleDropdown(key))
    },
    toggleSuggestionPanel: () => {
      dispatch(togglePhraseSuggestions())
    },
    undoEdit: (event) => {
      event.stopPropagation()
      dispatch(undoEdit())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(TransUnit)
