// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { includes } from 'lodash'
import DeleteEntry from './DeleteEntry'
import { Loader, Icon }
 from '../../components'
import { Label } from 'antd'
import { getLanguageUrl } from '../../utils/UrlHelper'

class Entry extends Component {
  static propTypes = {
    userLanguageTeams: PropTypes.object,
    locale: PropTypes.object.isRequired,
    permission: PropTypes.object.isRequired,
    isDeleting: PropTypes.bool,
    handleDelete: PropTypes.func
  }

  constructor () {
    super()
    this.state = {
      showDeleteModal: false
    }
  }

  setShowingDeleteEntryModal = (showing) => {
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

    const url = getLanguageUrl(localeDetails.localeId)

    return (
      <tr name='language-entry' className='bstrapReact'>
        <td>
          <a href={url} id={'language-name-' + localeDetails.localeId}>
            <span name='language-name'>
              {localeDetails.localeId} [{localeDetails.nativeName}]
            </span>
            {localeDetails.enabledByDefault &&
              <Label className='label-primary'>
                DEFAULT
              </Label>
            }
            {!localeDetails.enabled &&
              <Label className='label-info'>
                DISABLED
              </Label>
            }
            {isUserInTeam &&
              <Label className='label-success'>
                Member
              </Label>
            }
          </a>
          <br />
          <span className='languageCode'>
            {localeDetails.displayName}
          </span>
        </td>
        <td>
          <span className='u-textMuted'>
            <Icon name='user' className='s1' parentClassName='iconUser' />
            {locale.memberCount} &nbsp;
            {permission.canAddLocale &&
              <span>
                <Icon name='notification' className='s1'
                  parentClassName='iconUser' />
                {locale.requestCount}
              </span>
            }
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

export default Entry
