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
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import cx from 'classnames'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import SplitDropdown from './SplitDropdown'
import { Icon } from '../../components'
import ValidationErrorsModal from '../components/ValidationErrorsModal'
import { defaultSaveStatus, nonDefaultValidSaveStatuses, STATUS_REJECTED }
  from '../utils/status-util'
import { hasTranslationChanged } from '../utils/phrase-util'
import { toggleSaveErrorModal } from '../actions/phrases-actions'

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
  translated: <kbd>t</kbd>,
  approved: <kbd>a</kbd>,
  rejected: <kbd>r</kbd>
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
    showErrorModal: PropTypes.func.isRequired,
    saveDropdownKey: PropTypes.any.isRequired,
    openDropdown: PropTypes.any,
    saveAsMode: PropTypes.bool.isRequired,
    showSuggestions: PropTypes.bool.isRequired,
    suggestionSearchType: PropTypes.oneOf(['phrase', 'text']).isRequired,
    showRejectModal: PropTypes.func.isRequired,
    permissions: PropTypes.shape({
      reviewer: PropTypes.bool.isRequired,
      translator: PropTypes.bool.isRequired
    }).isRequired,
    validationMessages: PropTypes.any
  }

  componentWillMount () {
    const { toggleDropdown, saveDropdownKey } = this.props
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
  }

  // @ts-ignore any
  componentWillReceiveProps (nextProps) {
    const { toggleDropdown, saveDropdownKey, saveAsMode } = nextProps
    this.toggleDropdown = toggleDropdown.bind(undefined, saveDropdownKey)
    if (saveAsMode === true) {
      // @ts-ignore
      this.refs.saveTransDropdown.focus()
    }
  }

  // @ts-ignore any
  saveButtonElement = (status) => {
    const { phrase, saveAsMode, savePhraseWithStatus } = this.props
    const className = cx('EditorButton u-sizeHeight-1_1-4',
                         'u-sizeFull u-textLeft',
                         buttonClassByStatus[status])

    // @ts-ignore any
    const saveCallback = (event) => {
      if (status === STATUS_REJECTED) {
        this.props.showRejectModal()
      } else {
        savePhraseWithStatus(phrase, status, event)
      }
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
  // @ts-ignore any
  renderCountIconIfNonZero = ({count, active, onClick, iconName}) => {
    if (count === 0) {
      return undefined
    }

    return (
      <li className="u-sM-1-8">
        <Button
          className={cx('Button Button--snug Button--invisible u-roundish',
            { 'is-active': active })}
          onClick={onClick}>
          <span>
            <Icon name={iconName} className="s1" />
            <span className="u-textMini">
              {count}
            </span>
          </span>
        </Button>
      </li>
    )
  }

  cancelSave = () => {
    this.props.showErrorModal(this.props.phrase.id, false)
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
      showErrorModal,
      toggleSuggestionPanel,
      permissions,
      validationMessages
    } = this.props

    const dropdownIsOpen = openDropdown === saveDropdownKey || saveAsMode
    const translationHasChanged = hasTranslationChanged(phrase)
    const isSaving = !!phrase.inProgressSave
    const selectedButtonStatus = isSaving
      ? phrase.inProgressSave.status
      : defaultSaveStatus(phrase)
    // TODO translate "Saving..."
    const selectedButtonTitle =
      isSaving ? 'Saving...' : statusNames[selectedButtonStatus]
    // @ts-ignore any
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
    /* eslint-disable max-len */
    const saveAsLabel = translationHasChanged &&
      <span className="u-textMeta u-sMR-1-4 u-floatLeft u-sizeLineHeight-1_1-4">
        Save as
      </span>
    /* eslint-enable max-len */
    const actionButtonKeyShortcut =
      saveAsMode && statusShortcutKeys[selectedButtonStatus]
    const actionButton = (
      <Button
        className={cx('EditorButton u-sizeHeight-1_1-4 u-textCapitalize',
                      buttonClassByStatus[selectedButtonStatus])}
        disabled={isSaving || !translationHasChanged}
        onClick={saveCallback}>
        {selectedButtonTitle}{actionButtonKeyShortcut}
      </Button>
    )

    const otherStatuses = nonDefaultValidSaveStatuses(
      phrase, permissions)
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
                      'EditorDropdown-toggle',
                      buttonClassByStatus[selectedButtonStatus])}>
        <div className="Dropdown-toggleIcon">
          <Icon name="chevron-down" className="n2" title="Save as…" />
        </div>
      </Button>
      : undefined

    const otherActionButtonList = (
      /* eslint-disable max-len */
      <ul className="EditorDropdown-content EditorDropdown-content--bordered u-rounded">
        {otherActionButtons}
      </ul>
      /* eslint-enable max-len */
    )
    return (
      /* eslint-disable max-len */
      <React.Fragment>
        {validationMessages && validationMessages.errorCount > 0 &&
          <ValidationErrorsModal
            phrase={phrase}
            savePhraseWithStatus={savePhraseWithStatus}
            selectedButtonStatus={selectedButtonStatus}
            showErrorModal={showErrorModal}
            validationMessages={validationMessages}
          />}
        <div className="TransUnit-panelFooter u-cf TransUnit-panelFooter--translation">
          <div className="TransUnit-panelFooterLeftNav u-floatLeft u-sizeHeight-1_1-2">
            <ul className="u-listHorizontal">
              {suggestionsIcon}
              {glossaryIcon}
            </ul>
          </div>
          <div className="u-floatRight" ref="saveTransDropdown" tabIndex={0} >
            {saveAsLabel}
            <SplitDropdown
              onToggle={this.toggleDropdown}
              isOpen={dropdownIsOpen}
              actionButton={actionButton}
              toggleButton={dropdownToggleButton}
              content={otherActionButtonList} />
          </div>
        </div>
      </React.Fragment>
      /* eslint-enable max-len */
    )
  }
}

    // @ts-ignore any
function mapDispatchToProps (dispatch, _ownProps) {
  return {
    // @ts-ignore any
    showErrorModal: (phraseId, showValidationErrorModal) => {
      // @ts-ignore any
      dispatch(toggleSaveErrorModal(phraseId, showValidationErrorModal))
    }
  }
}

export default connect(null, mapDispatchToProps)(TransUnitTranslationFooter)
