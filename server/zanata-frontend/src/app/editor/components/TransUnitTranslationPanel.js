// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import Textarea from 'react-textarea-autosize'
import TransUnitTranslationHeader from './TransUnitTranslationHeader'
import TransUnitTranslationFooter from './TransUnitTranslationFooter'
import { LoaderText } from '../../components'
import { pick, isEmpty } from 'lodash'
import { phraseTextSelectionRange } from '../actions/phrases-actions'
import {
  getSyntaxHighlighting,
  getValidateHtmlXml,
  getValidateNewLine,
  getValidateTab,
  getValidateJavaVariables,
  getValidateXmlEntity,
  getValidatePrintfVariables,
  getValidatePrintfXsi
} from '../reducers'
import SyntaxHighlighter, { registerLanguage }
  from 'react-syntax-highlighter/light'
import Validation from './Validation/index.tsx'
import xml from 'react-syntax-highlighter/languages/hljs/xml'
import { atelierLakesideLight } from 'react-syntax-highlighter/styles/hljs'

registerLanguage('xml', xml)

const DO_NOT_RENDER = undefined

/**
 * Panel to display and edit translations of a phrase.
 */
class TransUnitTranslationPanel extends React.Component {
  static propTypes = {
    cancelEdit: PropTypes.func.isRequired,
    glossaryCount: PropTypes.number.isRequired,
    glossaryVisible: PropTypes.bool.isRequired,
    isRTL: PropTypes.bool.isRequired,
    onSelectionChange: PropTypes.func.isRequired,
    // the key of the currently open dropdown (may be undefined if none is open)
    openDropdown: PropTypes.any,
    permissions: PropTypes.shape({
      reviewer: PropTypes.bool.isRequired,
      translator: PropTypes.bool.isRequired
    }).isRequired,
    // FIXME use PropTypes.shape and include all used properties
    phrase: PropTypes.object.isRequired,
    saveAsMode: PropTypes.bool.isRequired,
    // the key for the save dropdown for this translation panel. Can be compared
    // with openDropdown to see whether this dropdown is open.
    saveDropdownKey: PropTypes.any.isRequired,
    savePhraseWithStatus: PropTypes.func.isRequired,
    selected: PropTypes.bool.isRequired,
    selectPhrasePluralIndex: PropTypes.func.isRequired,
    showSuggestions: PropTypes.bool.isRequired,
    suggestionCount: PropTypes.number.isRequired,
    suggestionSearchType: PropTypes.oneOf(['phrase', 'text']).isRequired,
    syntaxOn: PropTypes.bool.isRequired,
    textChanged: PropTypes.func.isRequired,
    toggleDropdown: PropTypes.func.isRequired,
    toggleGlossary: PropTypes.func.isRequired,
    toggleSuggestionPanel: PropTypes.func.isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired,
    toggleConcurrentModal: PropTypes.func.isRequired,
    validationOptions: PropTypes.any
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
        'toggleConcurrentModal',
        'toggleGlossary',
        'toggleSuggestionPanel',
        'showRejectModal',
        'permissions'
      ])
      footer = <TransUnitTranslationFooter {...footerProps} />
    }

    const {
      openDropdown,
      saveAsMode,
      saveDropdownKey,
      textChanged,
      permissions,
      validationOptions } = this.props
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
              syntaxOn={syntaxOn}
              validationOptions={validationOptions}
              permissions={permissions} />
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

export class TranslationItem extends React.Component {
  static propTypes = {
    dropdownIsOpen: PropTypes.bool.isRequired,
    index: PropTypes.number.isRequired,
    isPlural: PropTypes.bool.isRequired,
    onSelectionChange: PropTypes.func.isRequired,
    phrase: PropTypes.shape({
      id: PropTypes.any.isRequired,
      sources: PropTypes.any.isRequired
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
    validationOptions: PropTypes.any.isRequired,
    directionClass: PropTypes.string,
    syntaxOn: PropTypes.bool.isRequired,
    permissions: PropTypes.shape({
      reviewer: PropTypes.bool.isRequired,
      translator: PropTypes.bool.isRequired
    }).isRequired
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
      directionClass,
      permissions,
      phrase,
      validationOptions
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
    const syntaxHighlighter = (this.props.syntaxOn && selected)
      ? <SyntaxHighlighter
        language='html'
        style={atelierLakesideLight}
        wrapLines
        lineStyle={syntaxStyle}>
        {translation}
      </SyntaxHighlighter>
      : DO_NOT_RENDER
    const validation = (isEmpty(translation))
    ? DO_NOT_RENDER
    : <Validation
      source={phrase.sources[0]}
      target={translation}
      validationOptions={validationOptions} />
    const cantEditTranslation = !permissions.translator || dropdownIsOpen
    return (
      <div className="TransUnit-item" key={index}>
        {itemHeader}
        {/* TODO check that this does not trim strings */}
        {/* TODO translate "Enter a translation..." */}
        <Textarea
          ref={this.setTextArea}
          className={directionClass + ' TransUnit-text'}
          disabled={cantEditTranslation}
          rows={1}
          value={translation}
          placeholder="Enter a translation…"
          onFocus={this.setFocusedPlural}
          onChange={this._onChange}
          onSelect={onSelectionChange} />
        {syntaxHighlighter}
        {validation}
      </div>
    )
  }
}

function mapStateToProps (state) {
  const {ui, context, headerData} = state
  const targetLocaleDetails = ui.uiLocales[context.lang]
  return {
    syntaxOn: getSyntaxHighlighting(state),
    validationOptions: [
      {
        id: 'HTML_XML',
        label: 'HTML/XML tags',
        active: getValidateHtmlXml(state) === 'Warning',
        disabled: getValidateHtmlXml(state) === 'Error'
      },
      {
        id: 'NEW_LINE',
        label: 'Leading/trailing newline',
        active: getValidateNewLine(state) === 'Warning',
        disabled: getValidateNewLine(state) === 'Error'
      },
      {
        id: 'TAB',
        label: 'Tab characters',
        active: getValidateTab(state) === 'Warning',
        disabled: getValidateTab(state) === 'Error'
      },
      {
        id: 'JAVA_VARIABLES',
        label: 'Java variables',
        active: getValidateJavaVariables(state) === 'Warning',
        disabled: getValidateJavaVariables(state) === 'Error'
      },
      {
        id: 'XML_ENTITY',
        label: 'XML entity reference',
        active: getValidateXmlEntity(state) === 'Warning',
        disabled: getValidateXmlEntity(state) === 'Error'
      },
      {
        id: 'PRINTF_VARIABLES',
        label: 'Printf variables',
        active: getValidatePrintfVariables(state) === 'Warning',
        disabled: getValidatePrintfVariables(state) === 'Error'
      },
      {
        id: 'PRINTF_XSI_EXTENSION',
        label: 'Positional printf (XSI extension)',
        active: getValidatePrintfXsi(state) === 'Warning',
        disabled: getValidatePrintfXsi(state) === 'Error'
      }
    ],
    isRTL: targetLocaleDetails ? targetLocaleDetails.isRTL || false
        : false,
    permissions: headerData.permissions
  }
}

function mapDispatchToProps (dispatch, _ownProps) {
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
