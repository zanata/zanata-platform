import React from 'react'
import * as PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'
import {
  filterActivityPropType as idType
} from '../../utils/activity-util'
import { injectIntl, intlShape, defineMessages } from 'react-intl'

class ActivitySelectList extends React.Component {
  static propTypes = {
    intl: intlShape,
    selected: idType.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  render () {
    const {selected, selectItem, intl} = this.props
    const messages = defineMessages({
      all: { id: 'FilterButtons.all', defaultMessage: 'All' },
      comments: { id: 'FilterButtons.comments', defaultMessage: 'Comments' },
      updates: { id: 'FilterButtons.updates', defaultMessage: 'Updates' }
    })
    const filterButtons = [
      {
        id: 'all',
        icon: 'clock',
        label: intl.formatMessage(messages.all),
      },
      {
        id: 'comments',
        icon: 'comment',
        label: intl.formatMessage(messages.comments),
      },
      {
        id: 'updates',
        icon: 'refresh',
        label: intl.formatMessage(messages.updates),
      }
    ]
    return (
      <SelectButtonList items={filterButtons}
        className='Button--secondary filterButtons'
        selected={selected}
        selectItem={selectItem}
      />
    )
  }
}

ActivitySelectList.idType = idType

export default injectIntl(ActivitySelectList)
