import { values } from 'lodash'
import { encode } from '../utils/doc-id'
import Dropdown from './Dropdown'
import { Icon, Row } from 'zanata-ui'
import React, { PropTypes } from 'react'

/**
 * Dropdown to select the current language to translate to.
 */
const LanguagesDropdown = React.createClass({

  propTypes: {
    context: PropTypes.shape({
      projectVersion: PropTypes.shape({
        project: PropTypes.shape({
          slug: PropTypes.string
        }).isRequired,
        version: PropTypes.string.isRequired,
        locales: PropTypes.object.isRequired
      }).isRequired,
      selectedDoc: PropTypes.shape({
        id: PropTypes.string.isRequired
      }).isRequired,
      selectedLocale: PropTypes.string.isRequired
    }).isRequired,

    toggleDropdown: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired
  },

  localeUrl: function (locale) {
    const { projectVersion, selectedDoc } = this.props.context
    const docId = encode(selectedDoc.id)
    const project = projectVersion.project.slug
    const version = projectVersion.version
    // FIXME this URL is too much information to keep in this component.
    // FIXME loses any other query parameters
    return '/project/translate/' + project + '/v/' + version + '/' + docId +
      '?lang=' + locale.id
  },

  render: function () {
    const locales = this.props.context.projectVersion.locales
    const items = values(locales).map(locale => {
      const url = this.localeUrl(locale)
      return (
        <li key={locale.id}>
          <a href={url} className="Dropdown-item">
            {locale.name}
          </a>
        </li>
      )
    })

    // sometimes name is not yet available, fall back on id
    const selectedLocaleId = this.props.context.selectedLocale
    const selectedLocale = locales[selectedLocaleId]
    const localeName = selectedLocale && selectedLocale.name
      ? selectedLocale.name : selectedLocaleId
    return (
      <Dropdown onToggle={this.props.toggleDropdown}
        isOpen={this.props.isOpen}>
        <Dropdown.Button>
          <button className="Link--invert">
            <Row>
              {localeName}
              <div className="u-sML-1-8 Dropdown-toggleIcon">
                <Icon name="chevron-down" size="1" />
              </div>
            </Row>
          </button>
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

export default LanguagesDropdown
