import { values } from 'lodash'
import Dropdown from './Dropdown'
import React, { PropTypes } from 'react'

/**
 * Dropdown to select the language to display the user interface in.
 */
const UiLanguageDropdown = React.createClass({

  propTypes: {
    changeUiLocale: PropTypes.func.isRequired,
    selectedUiLocale: PropTypes.string,
    uiLocales: PropTypes.object.isRequired,

    toggleDropdown: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired
  },

  changeUiLocale: function (locale) {
    // AppCtrl expects { localeId, name } rather than { id, name }
    this.props.changeUiLocale({
      localeId: locale.id,
      name: locale.name
    })
  },

  render: function () {
    const items = values(this.props.uiLocales).map(locale => {
      return (
        <li key={locale.id}>
          <a onClick={() => this.changeUiLocale(locale)}
             className="Dropdown-item">
            {locale.name}
          </a>
        </li>
      )
    })

    const selectedLocaleId = this.props.selectedUiLocale
    const selectedLocale = this.props.uiLocales[selectedLocaleId]
    const uiLocaleName = selectedLocale ? selectedLocale.name : selectedLocaleId

    return (
      <Dropdown onToggle={this.props.toggleDropdown}
                isOpen={this.props.isOpen}
                className="Dropdown--right u-sMV-1-2">
        <Dropdown.Button>
          <a className="Link--invert u-inlineBlock u-textNoWrap u-sPH-1-4">
            {uiLocaleName}
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            {items}
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
  }
})

export default UiLanguageDropdown
