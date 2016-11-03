import React, { Component, PropTypes } from 'react'
import { includes } from 'lodash'
import DeleteEntry from './DeleteEntry'
import { Loader } from 'zanata-ui'

class Entry extends Component {
  constructor () {
    super()
    this.state = {
      showDeleteModal: false
    }
  }

  setShowingDeleteEntryModal (showing) {
    this.setState({
      showDeleteModal: showing
    })
  }

  render () {
    const {
      userLanguageTeams,
      locale,
      permission,
      isDeleting,
      handleDelete
    } = this.props

    const localeDetails = locale.localeDetails
    /* eslint-disable react/jsx-no-bind */
    const isUserInTeam = includes(userLanguageTeams, localeDetails.localeId)

    const url = window.config.baseUrl + '/language/view/' +
      localeDetails.localeId
    return (
      <tr name='language-entry'>
        <td>
          <a href={url} id={'language-name-' + localeDetails.localeId}>
            <span name='language-name'>
              {localeDetails.localeId} [{localeDetails.nativeName}]
            </span>
            {localeDetails.enabledByDefault &&
              <span className='greentext badge Mstart(rq)'>
                DEFAULT
              </span>
            }
            {!localeDetails.enabled &&
              <span className='dis badge Mstart(rq)'>
                DISABLED
              </span>
            }
            {isUserInTeam &&
              <span className='greentext badge Mstart(rq)'>
                Member
              </span>
            }
          </a>
          <br />
          <span className='langcode'>
            {localeDetails.displayName}
          </span>
        </td>
        <td>
          <span>
            <i className='fa fa-user'></i> {locale.memberCount}
          </span>
        </td>
        {permission.canDeleteLocale &&
          <td>
            {isDeleting
              ? <Loader />
              : <DeleteEntry locale={localeDetails}
                isDeleting={false}
                show={this.state.showDeleteModal}
                handleDeleteEntryDisplay={(display) =>
                    this.setShowingDeleteEntryModal(display)}
                handleDeleteEntry={handleDelete} />
            }
          </td>
        }
      </tr>
    )
    /* eslint-disable react/jsx-no-bind */
  }
}

Entry.propTypes = {
  userLanguageTeams: PropTypes.object,
  locale: PropTypes.object.isRequired,
  permission: PropTypes.object.isRequired,
  isDeleting: PropTypes.bool,
  handleDelete: PropTypes.func
}

export default Entry
