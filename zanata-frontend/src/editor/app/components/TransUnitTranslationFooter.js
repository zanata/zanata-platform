/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import React, { PropTypes } from 'react'
import cx from 'classnames'
import Button from './Button'
import SplitDropdown from './SplitDropdown'
import { Icon, Row } from 'zanata-ui'
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
    rejected: 'Button--warning',
    approved: 'Button--highlight'
  },

  statusNames: {
    untranslated: 'Untranslated',
    needswork: 'Needs Work',
    translated: 'Translated',
    rejected: 'Rejected',
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
            <Row>
              <Icon name="suggestions" />
              <span className="u-textMini">
                {suggestionCount}
              </span>
            </Row>
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
        <div className="Dropdown-toggleIcon">
          <Icon name="chevron-down" size="0" title="Save as…" />
        </div>
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
