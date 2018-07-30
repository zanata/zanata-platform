import React from 'react'
import * as PropTypes from 'prop-types'
import { values } from 'lodash'
import Dropdown from '../Dropdown'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'

/**
 * Dropdown to select the language to display the user interface in.
 */
class UiLanguageDropdown extends React.Component {
  static propTypes = {
    changeUiLocale: PropTypes.func.isRequired,
    selectedUiLocale: PropTypes.string,
    uiLocales: PropTypes.object.isRequired,

    toggleDropdown: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired
  }

  render () {
    const items = values(this.props.uiLocales).map(locale => {
      return (
        <LocaleItem key={locale.id}
          locale={locale}
          changeUiLocale={this.props.changeUiLocale} />
      )
    })

    const selectedLocaleId = this.props.selectedUiLocale
    const selectedLocale = this.props.uiLocales[selectedLocaleId]
    const uiDisplayName =
      selectedLocale ? selectedLocale.displayName : selectedLocaleId
    return (
      <Dropdown
        onToggle={this.props.toggleDropdown}
        isOpen={this.props.isOpen}
        className="Dropdown--right u-sMV-1-2">
        <Dropdown.Button>
          <a className="Link--invert u-inlineBlock u-textNoWrap u-sPH-1-4">
            <Icon type="global" className="mr1 white" />
            {uiDisplayName}
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
}

class LocaleItem extends React.Component {
  static propTypes = {
    locale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      displayName: PropTypes.string
    }).isRequired,
    changeUiLocale: PropTypes.func.isRequired
  }

  changeUiLocale = () => {
    const { id, name } = this.props.locale
    this.props.changeUiLocale({
      id,
      name
    })
  }

  render () {
    const { id, name, displayName } = this.props.locale
    return (
      <li key={id}>
        <a onClick={this.changeUiLocale}
          className="EditorDropdown-item"
          title={name} >
          {displayName}
        </a>
      </li>
    )
  }
}

export default UiLanguageDropdown
