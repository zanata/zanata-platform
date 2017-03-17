import Dropdown from './Dropdown'
import { Icon, Row } from 'zanata-ui'
import React, { PropTypes } from 'react'

/**
 * Dropdown to select the current document to work on.
 */
const DocsDropdown = React.createClass({

  propTypes: {
    context: PropTypes.shape({
      projectVersion: PropTypes.shape({
        project: PropTypes.shape({
          slug: PropTypes.string.isRequired
        }).isRequired,
        version: PropTypes.string.isRequired,
        docs: PropTypes.arrayOf(PropTypes.string)
      }).isRequired,
      selectedDoc: PropTypes.shape({
        id: PropTypes.string.isRequired
      }).isRequired,
      selectedLocale: PropTypes.string.isRequired
    }).isRequired,
    toggleDropdown: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired
  },

  docUrl: function (docId) {
    const { projectVersion, selectedLocale } = this.props.context
    const project = projectVersion.project.slug
    const version = projectVersion.version
    return '/project/translate/' + project + '/v/' + version + '/' + docId +
      '?lang=' + selectedLocale
  },

  render: function () {
    const ctx = this.props.context
    const selectedDoc = ctx.selectedDoc.id
    const items = ctx.projectVersion.docs.map(docId => {
      const url = this.docUrl(docId)
      // TODO highlight selected
      return (
        <li key={docId}>
          <a href={url} className="Dropdown-item">{docId}</a>
        </li>
      )
    })

    return (
      <Dropdown onToggle={this.props.toggleDropdown}
        isOpen={this.props.isOpen}>
        <Dropdown.Button>
          <button className="Link--invert">
            <Row>
              {selectedDoc}
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

export default DocsDropdown
