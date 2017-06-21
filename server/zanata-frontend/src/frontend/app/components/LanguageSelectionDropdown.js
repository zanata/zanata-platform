import React, {PropTypes, Component} from 'react'
import {MenuItem, DropdownButton} from 'react-bootstrap'

/**
 * Sub-component of language selection drop down
 * Handles behavior of language menu items
 */
class LanguageSelectionDropdown extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired,
    selectedLanguage: PropTypes.string.isRequired,
    locales: PropTypes.arrayOf(PropTypes.object).isRequired
  }
  render () {
    const {
      onClick,
      selectedLanguage,
      locales
    } = this.props
    const languageMenu = locales
        .map((locale, index) => {
          return (
            <LanguageMenuItem
              onClick={onClick}
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
    onClick: PropTypes.func.isRequired,
    eventKey: PropTypes.number.isRequired,
    selectedLanguage: PropTypes.string.isRequired
  }
  onClick = () => {
    this.props.onClick(this.props.language)
  }
  render () {
    const {
      language,
      eventKey,
      selectedLanguage
    } = this.props
    return (
      <MenuItem onClick={this.onClick}
        eventKey={eventKey} key={eventKey}
        active={language === selectedLanguage}>
        {language}
      </MenuItem>
    )
  }
}

export default LanguageSelectionDropdown
