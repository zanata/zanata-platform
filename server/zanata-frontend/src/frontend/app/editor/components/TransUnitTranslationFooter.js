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

import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import Button from './Button'
import SplitDropdown from './SplitDropdown'
import { Icon } from '../../components'
import { Row } from 'react-bootstrap'
import { defaultSaveStatus, nonDefaultValidSaveStatuses }
  from '../utils/status-util'
import { hasTranslationChanged } from '../utils/phrase-util'

const buttonClassByStatus = {
  untranslated: 'Button--neutral',
  needswork: 'Button--unsure',
  translated: 'Button--success',
  rejected: 'Button--warning',
  approved: 'Button--highlight'
}

const statusNames = {
  untranslated: 'Untranslated',
  needswork: 'Needs Work',
  translated: 'Translated',
  rejected: 'Rejected',
  approved: 'Approved'
}

const statusShortcutKeys = {
  needswork: <kbd>n</kbd>,
  translated: <kbd>t</kbd>
}

/**
 * Footer for translation with save buttons and other action widgets.
 */
class TransUnitTranslationFooter extends React.Component {
  static propTypes = {
    phrase: PropTypes.object.isRequired,
    glossaryCount: PropTypes.number.isRequired,
    glossaryVisible: PropTypes.bool.isRequired,
    toggleGlossary: PropTypes.func.isRequired,
    suggestionCount: PropTypes.number.isRequired,
    toggleSuggestionPanel: PropTypes.func.isRequired,
    savePhraseWithStatus: PropTypes.func.isRequired,
    toggleDropdown: PropTypes.func.isRequired,
    saveDropdownKey: PropTypes.any.isRequired,
    openDropdown: PropTypes.any,
    saveAsMode: PropTypes.bool.isRequired,
    showSuggestions: PropTypes.bool.isRequired,
    suggestionSearchType: PropTypes.oneOf(['phrase', 'text']).isRequired
  }

  componentWillMount () {
    const { toggleDropdown, saveDropdownKey } = this.props
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
  }

  componentWillReceiveProps (nextProps) {
    const { toggleDropdown, saveDropdownKey } = nextProps
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
  }

  saveButtonElement = (status) => {
    const { phrase, saveAsMode, savePhraseWithStatus } = this.props
    const className = cx('Button u-sizeHeight-1_1-4',
                         'u-sizeFull u-textLeft',
                         buttonClassByStatus[status])

    const saveCallback = (event) => {
      savePhraseWithStatus(phrase, status, event)
    }

    const shortcutKey = saveAsMode && statusShortcutKeys[status]

    return (
      <Button
        className={className}
        onClick={saveCallback}>
        {statusNames[status]}{shortcutKey}
      </Button>
    )
  }

  /* Icons for suggestion and glossary count */
  renderCountIconIfNonZero = ({count, active, onClick, iconName}) => {
    if (count === 0) {
      return undefined
    }

    return (
      <li className="u-sM-1-8">
        <Button
          className={cx('Button Button--snug Button--invisible u-roundish',
            { 'is-active': active })}
          title=" Suggestions available"
          onClick={onClick}>
          <Row>
            <Icon name={iconName} className="s1" />
            <span className="u-textMini">
              {count}
            </span>
          </Row>
        </Button>
      </li>
    )
  }

  render () {
    const {
      glossaryCount,
      glossaryVisible,
      openDropdown,
      phrase,
      saveAsMode,
      saveDropdownKey,
      savePhraseWithStatus,
      showSuggestions,
      suggestionCount,
      suggestionSearchType,
      toggleGlossary,
      toggleSuggestionPanel
    } = this.props

    const dropdownIsOpen = openDropdown === saveDropdownKey || saveAsMode
    const translationHasChanged = hasTranslationChanged(phrase)
    const isSaving = !!phrase.inProgressSave
    const selectedButtonStatus =
      isSaving ? phrase.inProgressSave.status : defaultSaveStatus(phrase)
    // TODO translate "Saving..."
    const selectedButtonTitle =
      isSaving ? 'Saving...' : statusNames[selectedButtonStatus]
    const saveCallback = isSaving ? undefined : (event) => {
      savePhraseWithStatus(phrase, selectedButtonStatus, event)
    }

    const suggestionsIcon = this.renderCountIconIfNonZero({
      count: suggestionCount,
      active: showSuggestions && suggestionSearchType === 'phrase',
      onClick: toggleSuggestionPanel,
      iconName: 'suggestions'
    })

    const glossaryIcon = this.renderCountIconIfNonZero({
      count: glossaryCount,
      active: glossaryVisible,
      onClick: toggleGlossary,
      iconName: 'glossary'
    })

    // TODO translate "Save as"
    const saveAsLabel = translationHasChanged &&
      <span className="u-textMeta u-sMR-1-4 u-floatLeft
                       u-sizeLineHeight-1_1-4">
          Save as
      </span>

    const actionButtonKeyShortcut =
      saveAsMode && statusShortcutKeys[selectedButtonStatus]
    const actionButton = (
      <Button
        className={cx('EditorButton u-sizeHeight-1_1-4 u-textCapitalize',
                      buttonClassByStatus[selectedButtonStatus])}
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
        className={cx('EditorButton Button--snug u-sizeHeight-1_1-4',
                      'Dropdown-toggle',
                      buttonClassByStatus[selectedButtonStatus])}
        title="Save as…">
        <div className="Dropdown-toggleIcon">
          <Icon name="chevron-down" className="s0" title="Save as…" />
        </div>
      </Button>
      : undefined

    const otherActionButtonList = (
      <ul className="EditorDropdown-content Dropdown-content--bordered
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
            {glossaryIcon}
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
}

export default TransUnitTranslationFooter
