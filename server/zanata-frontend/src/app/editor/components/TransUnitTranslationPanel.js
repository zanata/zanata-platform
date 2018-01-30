import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import Textarea from 'react-textarea-autosize'
import TransUnitTranslationHeader from './TransUnitTranslationHeader'
import TransUnitTranslationFooter from './TransUnitTranslationFooter'
import { LoaderText } from '../../components'
import { pick } from 'lodash'
import { phraseTextSelectionRange } from '../actions/phrases-actions'
import { getSyntaxHighlighting } from '../reducers'
import SyntaxHighlighter from 'react-syntax-highlighter'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'
/**
 * Panel to display and edit translations of a phrase.
 */
class TransUnitTranslationPanel extends React.Component {
  static propTypes = {
    glossaryCount: PropTypes.number.isRequired,
    glossaryVisible: PropTypes.bool.isRequired,
    // the key of the currently open dropdown (may be undefined if none is open)
    openDropdown: PropTypes.any,
    onSelectionChange: PropTypes.func.isRequired,
    // the key for the save dropdown for this translation panel. Can be compared
    // with openDropdown to see whether this dropdown is open.
    saveDropdownKey: PropTypes.any.isRequired,
    selected: PropTypes.bool.isRequired,
    // FIXME use PropTypes.shape and include all used properties
    phrase: PropTypes.object.isRequired,
    savePhraseWithStatus: PropTypes.func.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    undoEdit: PropTypes.func.isRequired,
    toggleDropdown: PropTypes.func.isRequired,
    textChanged: PropTypes.func.isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired,
    saveAsMode: PropTypes.bool.isRequired,
    selectPhrasePluralIndex: PropTypes.func.isRequired,
    suggestionCount: PropTypes.number.isRequired,
    showSuggestions: PropTypes.bool.isRequired,
    toggleGlossary: PropTypes.func.isRequired,
    toggleSuggestionPanel: PropTypes.func.isRequired,
    suggestionSearchType: PropTypes.oneOf(['phrase', 'text']).isRequired,
    isRTL: PropTypes.bool.isRequired,
    syntaxOn: PropTypes.bool.isRequired
  }

  componentWillMount () {
    // will be set by refs, initialize here to avoid need for null checks
    this.textareas = []
    // indicates when a textarea should be focused but was not rendered yet
    // so should focus as soon as the rendered textarea is available
    this.shouldFocus = false
  }

  componentDidMount () {
    const { selected, phrase } = this.props
    if (selected) {
      // this is the selected row, focus if the textarea is available
      // (should be available but in practice the ref is not set until later)
      const selectedIndex = phrase.selectedPluralIndex || 0
      const textarea = this.textareas[selectedIndex]
      if (textarea) {
        textarea.focus()
      } else {
        this.shouldFocus = true
      }
    }
  }

  componentDidUpdate (prevProps) {
    const { phrase } = this.props
    const becameSelected = this.props.selected && !prevProps.selected
    // protective check to prevent repeatedly gaining focus
    // phrase.shouldGainFocus has a different truthy value if it has been set
    // again
    const shouldGainFocus = phrase.shouldGainFocus &&
      phrase.shouldGainFocus !== prevProps.phrase.shouldGainFocus

    if (becameSelected || shouldGainFocus || this.shouldFocus) {
      const selectedIndex = phrase.selectedPluralIndex || 0
      const textarea = this.textareas[selectedIndex]

      if (textarea) {
        textarea.focus()
        this.shouldFocus = false
      } else {
        this.shouldFocus = true
      }
    }
  }

  setTextArea = (index, ref) => {
    this.textareas[index] = ref
  }

  render () {
    const {
      onSelectionChange,
      phrase,
      selected,
      selectPhrasePluralIndex,
      isRTL,
      syntaxOn
    } = this.props
    var header, footer
    const isPlural = phrase.plural
    const directionClass = isRTL ? 'rtl' : 'ltr'

    if (selected) {
      const headerProps = pick(this.props, [
        'cancelEdit',
        'phrase',
        'translationLocale',
        'undoEdit'
      ])
      header = <TransUnitTranslationHeader {...headerProps} />

      const footerProps = pick(this.props, [
        'glossaryCount',
        'glossaryVisible',
        'openDropdown',
        'phrase',
        'saveAsMode',
        'saveDropdownKey',
        'savePhraseWithStatus',
        'showSuggestions',
        'suggestionCount',
        'suggestionSearchType',
        'toggleDropdown',
        'toggleGlossary',
        'toggleSuggestionPanel'
      ])
      footer = <TransUnitTranslationFooter {...footerProps} />
    }

    const {
      openDropdown,
      saveAsMode,
      saveDropdownKey,
      textChanged } = this.props
    const dropdownIsOpen = openDropdown === saveDropdownKey || saveAsMode

    // TODO use dedicated phrase.isLoading variable when available
    const isLoading = !phrase.newTranslations
    const selectedPluralIndex = phrase.selectedPluralIndex || 0

    let translations

    if (isLoading) {
      translations = <span className="u-textMeta">
        <LoaderText loading />
      </span>
    } else {
      const newTranslations = phrase.newTranslations
      ? phrase.newTranslations
      : ['Loading...']

      translations = newTranslations.map(
        (translation, index) => {
          return (
            <TranslationItem key={index}
              dropdownIsOpen={dropdownIsOpen}
              index={index}
              isPlural={isPlural}
              phrase={phrase}
              onSelectionChange={onSelectionChange}
              selected={selected}
              selectedPluralIndex={selectedPluralIndex}
              selectPhrasePluralIndex={selectPhrasePluralIndex}
              setTextArea={this.setTextArea}
              textChanged={textChanged}
              translation={translation}
              directionClass={directionClass}
              syntaxOn={syntaxOn} />
          )
        })
    }

    return (
      <div className="TransUnit-panel TransUnit-translation">
        {header}
        <span className={directionClass}>{translations}</span>
        {footer}
      </div>
    )
  }
}

class TranslationItem extends React.Component {
  static propTypes = {
    dropdownIsOpen: PropTypes.bool.isRequired,
    index: PropTypes.number.isRequired,
    isPlural: PropTypes.bool.isRequired,
    onSelectionChange: PropTypes.func.isRequired,
    phrase: PropTypes.shape({
      id: PropTypes.any.isRequired
    }).isRequired,
    selected: PropTypes.bool.isRequired,
    selectedPluralIndex: PropTypes.number.isRequired,
    selectPhrasePluralIndex: PropTypes.func.isRequired,
    /* Set a reference to the textarea component, for use with focus
     * setTextArea(index, ref)
     */
    setTextArea: PropTypes.func.isRequired,
    textChanged: PropTypes.func.isRequired,
    translation: PropTypes.string,
    directionClass: PropTypes.string,
    syntaxOn: PropTypes.bool.isRequired
  }

  setTextArea = (ref) => {
    this.props.setTextArea(this.props.index, ref)
  }

  _onChange = (event) => {
    const { index, phrase, textChanged } = this.props
    textChanged(phrase.id, index, event)
  }

  setFocusedPlural = () => {
    const { index, phrase, selectPhrasePluralIndex } = this.props
    selectPhrasePluralIndex(phrase.id, index)
  }

  render () {
    const {
      dropdownIsOpen,
      index,
      isPlural,
      onSelectionChange,
      selected,
      selectedPluralIndex,
      translation,
      directionClass
    } = this.props

    // TODO make this translatable
    const headerLabel = index === 0
     ? 'Singular Form'
     : 'Plural Form'

    const highlightHeader = selected && index === selectedPluralIndex
    const headerClass = highlightHeader
      ? 'u-textMini u-textPrimary'
      : 'u-textMeta'

    const itemHeader = isPlural &&
      <div className="TransUnit-itemHeader">
        <span className={headerClass}>
          {headerLabel}
        </span>
      </div>
    const syntaxStyle = {
      padding: '0.5rem',
      width: '90%',
      whiteSpace: 'pre-wrap',
      wordWrap: 'break-word'
    }
    const syntaxHighlighter = this.props.syntaxOn
      ? <SyntaxHighlighter
        language='html'
        style={atelierLakesideLight}
        wrapLines
        lineStyle={syntaxStyle}>
        {translation}
      </SyntaxHighlighter>
      : ''
    return (
      <div className="TransUnit-item" key={index}>
        {itemHeader}
        {/* TODO check that this does not trim strings */}
        {/* TODO translate "Enter a translation..." */}
        <Textarea
          ref={this.setTextArea}
          className={directionClass + ' TransUnit-text'}
          disabled={dropdownIsOpen}
          rows={1}
          value={translation}
          placeholder="Enter a translation…"
          onFocus={this.setFocusedPlural}
          onChange={this._onChange}
          onSelect={onSelectionChange} />
        {syntaxHighlighter}
      </div>
    )
  }
}

function mapStateToProps (state) {
  const {ui, context} = state
  const targetLocaleDetails = ui.uiLocales[context.lang]
  return {
    syntaxOn: getSyntaxHighlighting(state),
    isRTL: targetLocaleDetails ? targetLocaleDetails.isRTL || false
        : false
  }
}

function mapDispatchToProps (dispatch, ownProps) {
  // TODO put all the branch-specific stuff here for a start
  return {
    onSelectionChange: (event) => {
      const { selectionStart, selectionEnd } = event.target
      event.stopPropagation()
      // This does seem to fire when selected phrase changes, so it is fine
      // to just transmit the range without info about which row it is for.
      dispatch(phraseTextSelectionRange(selectionStart, selectionEnd))
    }
  }
}

export default connect(
    mapStateToProps, mapDispatchToProps)(TransUnitTranslationPanel)
