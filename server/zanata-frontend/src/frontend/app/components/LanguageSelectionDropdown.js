import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {MenuItem, DropdownButton} from 'react-bootstrap'
import {LocaleType} from '../utils/prop-types-util.js'

/**
 * Root component for Language Selection Dropdown
 */
class LanguageSelectionDropdown extends Component {
  static propTypes = {
    /* params: selectedLanguage */
    selectLanguage: PropTypes.func.isRequired,
    selectedLanguage: PropTypes.string.isRequired,
    locales: PropTypes.arrayOf(LocaleType).isRequired
  }
  render () {
    const {
      selectLanguage,
      selectedLanguage,
      locales
    } = this.props
    const languageMenu = locales
        .map((locale, index) => {
          return (
            <LanguageMenuItem
              selectLanguage={selectLanguage}
              language={locale.displayName}
              selectedLanguage={selectedLanguage}
              eventKey={index}
              key={index}
            />
          )
        })
    return (
      <DropdownButton bsStyle='default' bsSize='small'
        title={selectedLanguage}
        id='dropdown-basic'
        className='vmerge-ddown'>
        {languageMenu}
      </DropdownButton>
    )
  }
}

/**
 * Sub-component of language selection drop down
 * Handles behavior of language menu items
 */
class LanguageMenuItem extends Component {
  static propTypes = {
    language: PropTypes.string.isRequired,
    selectLanguage: PropTypes.func.isRequired,
    eventKey: PropTypes.number.isRequired,
    selectedLanguage: PropTypes.string.isRequired
  }
  selectLanguage = () => {
    this.props.selectLanguage(this.props.language)
  }
  render () {
    const {
      language,
      eventKey,
      selectedLanguage
    } = this.props
    return (
      <MenuItem onClick={this.selectLanguage}
        eventKey={eventKey} key={eventKey}
        active={language === selectedLanguage}>
        {language}
      </MenuItem>
    )
  }
}

export default LanguageSelectionDropdown
