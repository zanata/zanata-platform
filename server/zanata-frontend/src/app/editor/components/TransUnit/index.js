import * as React from 'react'
import * as PropTypes from 'prop-types'
import cx from 'classnames'
import { GLOSSARY_TAB } from '../../reducers/ui-reducer'
import { getSuggestionsPanelVisible } from '../../reducers'
import TransUnitStatus from '../TransUnitStatus'
import TransUnitSourcePanel from '../TransUnitSourcePanel'
import TransUnitTranslationPanel from '../TransUnitTranslationPanel'
import { connect } from 'react-redux'
import { pick } from 'lodash'
import { toggleDropdown, closeDropdown } from '../../actions'
import { toggleGlossary } from '../../actions/header-actions'
import {
  cancelEdit,
  copyFromSource,
  savePhraseWithStatus,
  selectPhrase,
  selectPhrasePluralIndex,
  translationTextInputChanged,
  undoEdit
} from '../../actions/phrases-actions'
import { togglePhraseSuggestions } from '../../actions/suggestions-actions'
import RejectTranslationModal from '../../containers/RejectTranslationModal'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'

const transUnitClassByStatus = {
  untranslated: 'TransUnit--neutral',
  needswork: 'TransUnit--unsure',
  translated: 'TransUnit--success',
  approved: 'TransUnit--highlight',
  rejected: 'TransUnit--warning'
}

/**
 * Single row in the editor displaying a whole phrase.
 * Including source, translation, metadata and editing
 * facilities.
 */
class TransUnit extends React.Component {
  static propTypes = {
    glossaryCount: PropTypes.number.isRequired,
    glossaryVisible: PropTypes.bool.isRequired,
    toggleGlossary: PropTypes.func.isRequired,
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
    selected: PropTypes.bool.isRequired,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    }))
  }

  constructor (props) {
    super(props)
    this.state = {
      saveDropdownKey: {},
      showRejectModal: false
    }
  }

  selectPhrase = () => {
    const { phrase, selectPhrase } = this.props
    selectPhrase(phrase.id)
  }

  toggleRejectModal = () => {
    this.setState(prevState => ({
      showRejectModal: !prevState.showRejectModal
    }))
  }

  render () {
    // TODO different display if isLoading (need to add or infer isLoading)

    const displayStatus = this.props.phrase.inProgressSave
      ? this.props.phrase.inProgressSave.status
      : this.props.phrase.status

    const className = cx('TransUnit',
      transUnitClassByStatus[displayStatus],
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
      'glossaryCount',
      'glossaryVisible',
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
      'toggleGlossary',
      'toggleSuggestionPanel',
      'translationLocale',
      'undoEdit'
    ])

    // Only Load the Reject Translation Modal when it is to be opened
    const rejectTranslationModal = this.state.showRejectModal
       ? <RejectTranslationModal
         show={this.state.showRejectModal}
         onHide={this.toggleRejectModal}
         transUnitID={this.props.phrase.id}
         revision={this.props.phrase.revision}
         localeId={this.props.translationLocale.id}
         criteria={this.props.criteria} />
       : undefined
    return (
      <div>
        <div className={className}
          onClick={this.selectPhrase}>
          <TransUnitStatus phrase={this.props.phrase} />
          <TransUnitSourcePanel {...phraseSourcePanelProps} />
          <TransUnitTranslationPanel {...phraseTranslationPanelProps}
            saveDropdownKey={this.props.phrase.id}
            showRejectModal={this.toggleRejectModal} />
        </div>
        {rejectTranslationModal}
      </div>
    )
  }
}

/**
 * Get the count of available glossary terms for this phrase.
 *
 * Returns 0 if thre is no way to look up the results.
 *
 * @param phrase containing source text used for glossary search
 * @param glossary containing cached search results
 * @returns {number} count of available results
 */
function countGlossaryResults (phrase, glossary) {
  if (!phrase.sources) {
    // phrase detail not loaded, no way to look up any results
    return 0
  }
  // Search text that this phrase would generate for glossary search
  const glossarySearchText = phrase.sources.join(' ')
  const glossaryResults = glossary.results.get(glossarySearchText)

  return glossaryResults ? glossaryResults.length : 0
}

function mapStateToProps (state, ownProps) {
  const { index, phrase } = ownProps
  const { glossary, suggestions, ui } = state
  const { sidebar } = ui.panels
  const sourceLocale = state.context.sourceLocale

  const glossaryCount = countGlossaryResults(phrase, glossary)
  const glossaryVisible =
    sidebar.visible && sidebar.selectedTab === GLOSSARY_TAB

  // FIXME do not want to show 0 while it is loading, maybe show nothing or show
  //       that the suggestion search is in progress if phraseSearch.loading
  const phraseSearch = suggestions.searchByPhrase[phrase.id]
  const suggestionCount = phraseSearch ? phraseSearch.suggestions.length : 0
  const suggestionSearchType = suggestions.searchType
  const showSuggestions = getSuggestionsPanelVisible(state)

  const passThroughProps = pick(state, [
    'openDropdown'
  ])

  const saveAsMode = state.phrases.saveAsMode
  const criteria = state.review.criteria

  return {
    ...passThroughProps,
    // TODO add something for looking up locale name instead, or check
    //      whether it comes with the response.
    criteria: criteria,
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
    glossaryCount,
    glossaryVisible,
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
    toggleGlossary: () => {
      dispatch(toggleGlossary())
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
