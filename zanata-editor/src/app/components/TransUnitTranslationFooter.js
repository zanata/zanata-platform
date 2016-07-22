import React, { PropTypes } from 'react'
import cx from 'classnames'
import Button from './Button'
import SplitDropdown from './SplitDropdown'
import Icon from './Icon'
import { defaultSaveStatus, nonDefaultValidSaveStatuses } from '../utils/status'
import { hasTranslationChanged } from '../utils/phrase'

/**
 * Footer for translation with save buttons and other action widgets.
 */
const TransUnitTranslationFooter = React.createClass({

  propTypes: {
    phrase: PropTypes.object.isRequired,
    suggestionCount: PropTypes.number.isRequired,
    toggleSuggestionPanel: PropTypes.func.isRequired,
    savePhraseWithStatus: PropTypes.func.isRequired,
    toggleDropdown: PropTypes.func.isRequired,
    saveDropdownKey: PropTypes.any.isRequired,
    openDropdown: PropTypes.any,
    saveAsMode: PropTypes.bool.isRequired,
    showSuggestions: PropTypes.bool.isRequired,
    suggestionSearchType: PropTypes.oneOf(['phrase', 'text']).isRequired
  },

  buttonClassByStatus: {
    untranslated: 'Button--neutral',
    needswork: 'Button--unsure',
    translated: 'Button--success',
    approved: 'Button--highlight'
  },

  statusNames: {
    untranslated: 'Untranslated',
    needswork: 'Needs Work',
    translated: 'Translated',
    approved: 'Approved'
  },

  statusShortcutKeys: {
    needswork: <kbd>n</kbd>,
    translated: <kbd>t</kbd>
  },

  componentWillMount: function () {
    const { toggleDropdown, saveDropdownKey } = this.props
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
  },
  componentWillReceiveProps: function (nextProps) {
    const { toggleDropdown, saveDropdownKey } = nextProps
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
  },

  saveButtonElement: function (status) {
    const { phrase, saveAsMode, savePhraseWithStatus } = this.props
    const className = cx('Button u-sizeHeight-1_1-4',
                         'u-sizeFull u-textLeft',
                         this.buttonClassByStatus[status])

    const saveCallback = (event) => {
      savePhraseWithStatus(phrase, status, event)
    }

    const shortcutKey = saveAsMode && this.statusShortcutKeys[status]

    return (
      <Button
        className={className}
        onClick={saveCallback}>
        {this.statusNames[status]}{shortcutKey}
      </Button>
    )
  },

  render: function () {
    const { openDropdown, phrase, saveAsMode, saveDropdownKey,
      savePhraseWithStatus, showSuggestions, suggestionCount,
      suggestionSearchType, toggleSuggestionPanel } = this.props

    const dropdownIsOpen = openDropdown === saveDropdownKey || saveAsMode
    const translationHasChanged = hasTranslationChanged(phrase)
    const isSaving = !!phrase.inProgressSave
    const selectedButtonStatus =
      isSaving ? phrase.inProgressSave.status : defaultSaveStatus(phrase)
    // TODO translate "Saving..."
    const selectedButtonTitle =
      isSaving ? 'Saving...' : this.statusNames[selectedButtonStatus]
    const saveCallback = isSaving ? undefined : (event) => {
      savePhraseWithStatus(phrase, selectedButtonStatus, event)
    }

    var suggestionsIcon
    if (suggestionCount > 0) {
      const isPhraseSearchActive = showSuggestions &&
        suggestionSearchType === 'phrase'
      const iconClasses = cx('Button Button--snug Button--invisible u-roundish',
       { 'is-active': isPhraseSearchActive })

      suggestionsIcon = (
        <li>
          <Button
            className={iconClasses}
            title="Suggestions available"
            onClick={toggleSuggestionPanel}>
            <Icon name="suggestions" />
            <span className="u-textMini">
              {suggestionCount}
            </span>
          </Button>
        </li>
      )
    }

    // TODO translate "Save as"
    const saveAsLabel = translationHasChanged &&
      <span className="u-textMeta u-sMR-1-4 u-floatLeft
                       u-sizeLineHeight-1_1-4">
          Save as
      </span>

    const actionButtonKeyShortcut =
      saveAsMode && this.statusShortcutKeys[selectedButtonStatus]
    const actionButton = (
      <Button
        className={cx('Button u-sizeHeight-1_1-4 u-textCapitalize',
                      this.buttonClassByStatus[selectedButtonStatus])}
        disabled={isSaving || !translationHasChanged}
        title={selectedButtonTitle}
        onClick={saveCallback}>
        {selectedButtonTitle}{actionButtonKeyShortcut}
      </Button>
    )

    const otherStatuses = nonDefaultValidSaveStatuses(phrase)
    const otherActionButtons = otherStatuses.map((status, index) => {
      return (
        <li key={index}>
          {this.saveButtonElement(status)}
        </li>
      )
    })

    const dropdownToggleButton = otherStatuses.length > 0
      ? <Button
        className={cx('Button Button--snug u-sizeHeight-1_1-4',
                      'Dropdown-toggle',
                      this.buttonClassByStatus[selectedButtonStatus])}
        title="Save as…">
        <Icon name="chevron-down"
          title="Save as…"
          className="Icon--sm Dropdown-toggleIcon" />
      </Button>
      : undefined

    const otherActionButtonList = (
      <ul className="Dropdown-content Dropdown-content--bordered
                     u-rounded">
        {otherActionButtons}
      </ul>
    )

    return (
      <div className="TransUnit-panelFooter u-cf
                      TransUnit-panelFooter--translation">
        <div className="TransUnit-panelFooterLeftNav u-floatLeft
                        u-sizeHeight-1_1-2">
          <ul className="u-listHorizontal">
{/* don't think this was ever displayed
            <li class="u-gtemd-hidden" ng-show="appCtrl.PRODUCTION">
              <button class="Link Link--neutral u-sizeHeight-1_1-2"
                title="{{::'Details'|translate}}">
                <icon name="info" title="{{::'Details'|translate}}"
                      class="u-sizeWidth-1_1-2"></icon>
              </button>
            </li>
*/}
            {suggestionsIcon}
          </ul>
        </div>
        <div className="u-floatRight">
          {saveAsLabel}
          <SplitDropdown
            onToggle={this.toggleDropdown}
            isOpen={dropdownIsOpen}
            actionButton={actionButton}
            toggleButton={dropdownToggleButton}
            content={otherActionButtonList} />
        </div>
      </div>
    )
  }
})

export default TransUnitTranslationFooter
