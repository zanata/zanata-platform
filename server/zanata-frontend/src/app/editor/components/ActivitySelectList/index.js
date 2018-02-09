import React from 'react'
import * as PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'

const items = [
  {
    id: 'all',
    icon: 'clock',
    label: 'All'
  },
  {
    id: 'comments',
    icon: 'comment',
    label: 'Comments'
  },
  {
    id: 'updates',
    icon: 'refresh',
    label: 'Updates'
  }
]

const idType = PropTypes.oneOf(['all', 'comments', 'updates'])

class ActivitySelectList extends React.Component {
  static propTypes = {
    selected: idType.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={items}
        className="Button--secondary" selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

ActivitySelectList.idType = idType

export default ActivitySelectList
